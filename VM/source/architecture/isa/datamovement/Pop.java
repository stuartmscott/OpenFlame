/*
 * The OpenFlame Project <http://stuartmscott.github.io/OpenFlame/>.
 *
 * Copyright (C) 2015 OpenFlame Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package architecture.isa.datamovement;

import java.util.BitSet;

import architecture.IContext;
import architecture.isa.Instruction;
import architecture.isa.special.Interrupt;

public class Pop extends Instruction {

    public volatile boolean mIssued = false;
    public volatile int mRegisterIndex = 0;
    public final BitSet mMask;

    public Pop(IContext context, long mask) {
        super(context, "Pop");
        // Registers are popped in descending index
        mMask = BitSet.valueOf(new long[] { mask });
        trace("Pop");
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(3) - 1);// Load stack pointer
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
        if (mPipeline.getLoadRegister0() < mContext.getStackStart()) {
            mContext.error(Interrupt.STACK_UNDERFLOW);
        } else if (!mCard.getDataStore().isBusy() && !mIssued) {
            mCard.getDataStore().read(mPipeline.getLoadRegister0());
            mIssued = true;
        }
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
        mContext.setRetry(!mIssued || !mCard.getDataStore().success() || mCard.getDataStore().isBusy());
        if (!mContext.isRetry()) {
            mPipeline.setFormatRegister1(mCard.getDataStore().getBus().mValues[0]);
        } else if (mIssued || !mCard.getDataStore().success() || !mCard.getDataStore().isBusy()) {
            mIssued = false;// Cache miss, try again
        }
    }

    public void store() {
        if (!mContext.isRetry()) {
            mRegisterIndex = mMask.nextSetBit(mRegisterIndex);
            mMask.clear(mRegisterIndex);
            mContext.getRegisterBank().write(3, mPipeline.getFormatRegister0());// Store new stack pointer
            mContext.getRegisterBank().write(63 - mRegisterIndex, mPipeline.getFormatRegister1());// Store value
            mContext.setRetry(mMask.cardinality() > 0);
            mIssued = false;
        }
    }

}
