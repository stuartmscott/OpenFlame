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
package architecture.isa.special;

import architecture.IContext;

public class InterruptReturn extends Special {

    private final int mReturnAddressRegister;

    public InterruptReturn(IContext context, int returnAddressRegister) {
        super(context, "InterruptReturn");
        mReturnAddressRegister = returnAddressRegister;
        mIncrementProgramCounter = false;
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mReturnAddressRegister));
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
    }

    public void store() {
        mContext.setIsHandlingInterrupt(false);
        mContext.getRegisterBank().write(2, mPipeline.getFormatRegister0());//Write new pc
    }

}
