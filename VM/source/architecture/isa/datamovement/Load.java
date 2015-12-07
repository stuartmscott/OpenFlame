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

public class Load extends Instruction {

    public final int mBaseRegister;
    public final long mOffset;
    public final int mDestinationRegister;
    public volatile boolean mIssued = false;

    public Load(IContext context, int baseRegister, long offset, int destinationReg) {
        super(context, "Load");
        this.mBaseRegister = baseRegister;
        this.mOffset = offset;
        this.mDestinationRegister = destinationReg;
        trace("Load " + baseRegister + " " + offset + " " + destinationReg);
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mBaseRegister) + mOffset);
    }

    public void execute() {
        if (!mCard.getDataStore().isBusy() && !mIssued) {
            mCard.getDataStore().read(mPipeline.getLoadRegister0());
            mIssued = true;
        }
    }

    public void format() {
        mContext.setRetry(!mIssued || !mCard.getDataStore().success() || mCard.getDataStore().isBusy());
        if (!mContext.isRetry()) {
            mPipeline.setFormatRegister0(mCard.getDataStore().getBus().mValues[0]);
        } else if (mIssued || !mCard.getDataStore().success() || !mCard.getDataStore().isBusy()) {
            mIssued = false;// Cache miss, try again
        }
    }

    public void store() {
        if (!mContext.isRetry()) {
            mContext.getRegisterBank().write(mDestinationRegister, mPipeline.getFormatRegister0());
        }
    }

}
