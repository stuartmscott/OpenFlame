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

public class Push extends Instruction {

    public volatile boolean mIssued = false;
    public volatile int mRegisterIndex = 64;
    public final BitSet mMask;

    public Push(IContext context, long mask) {
        super(context, "Push");
        // Registers are pushed in ascending index
        mMask = BitSet.valueOf(new long[] { mask });
        trace("Push");
    }

    public void load() {
        mRegisterIndex = mMask.previousSetBit(mRegisterIndex);
        mMask.clear(mRegisterIndex);
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(3));// Load stack pointer
        mPipeline.setLoadRegister1(mContext.getRegisterBank().read(63 - mRegisterIndex));
        mIssued = false;
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
        if (mPipeline.getLoadRegister0() >= mContext.getStackLimit()) {
            mContext.error(Interrupt.STACK_OVERFLOW);
        } else if (!mCard.getDataStore().isBusy() && !mIssued) {
            mCard.getDataStore().write(mPipeline.getLoadRegister0(), mPipeline.getLoadRegister1());
            mIssued = true;
        }
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
        mContext.setRetry(!mIssued || !mCard.getDataStore().success() || mCard.getDataStore().isBusy());
    }

    public void store() {
        if (!mContext.isRetry()) {
            mContext.getRegisterBank().write(3, mPipeline.getFormatRegister0() + 1);// Store new stack pointer
            mContext.setRetry(mMask.cardinality() > 0);
        }
    }

}
