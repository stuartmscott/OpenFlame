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

import architecture.IContext;
import architecture.isa.Instruction;

public class Store extends Instruction {

    public final int mBaseRegister;
    public final long mOffset;
    public final int mSourceRegister;
    public volatile boolean mIssued = false;

    public Store(IContext context, int baseRegister, long offset, int sourceReg) {
        super(context, "Store");
        this.mBaseRegister = baseRegister;
        this.mOffset = offset;
        this.mSourceRegister = sourceReg;
        trace("Store " + baseRegister + " " + offset + " " + sourceReg);
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mBaseRegister) + mOffset);
        mPipeline.setLoadRegister1(mContext.getRegisterBank().read(mSourceRegister));
    }

    public void execute() {
        if (!mCard.getDataStore().isBusy() && !mIssued) {
            mCard.getDataStore().write(mPipeline.getLoadRegister0(), mPipeline.getLoadRegister1());
            mIssued = true;
        }
    }

    public void format() {
        mContext.setRetry(!mIssued || !mCard.getDataStore().success() || mCard.getDataStore().isBusy());
        if (mContext.isRetry())
            mIssued = false;
    }

    public void store() {
        // Do nothing
    }

}
