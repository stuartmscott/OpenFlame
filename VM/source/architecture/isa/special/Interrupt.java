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

public class Interrupt extends Special {

    public static final int SIGNAL_RECEIVED = 0;
    public static final int ERROR_OCCURED = 1;

    //Errors
    public static final int UNSUPPORTED_OPERATION = 0;
    public static final int ARITHMETIC_ERROR = 1;
    public static final int REG_ACCESS_ERROR = 2;
    public static final int MEM_ACCESS_ERROR = 3;
    public static final int STACK_OVERFLOW = 4;
    public static final int STACK_UNDERFLOW = 5;

    private final long mInterruptId;

    public Interrupt(IContext context, long interruptId) {
        super(context, "Interrupt");
        mInterruptId = interruptId;
        mIncrementProgramCounter = false;
        context.setIsHandlingInterrupt(true);
    }

    public void load() {
        mPipeline.setLoadRegister0(mInterruptId);
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
    }

    public void store() {
        mContext.getRegisterBank().write(2, mPipeline.getFormatRegister0() + mCard.getInterruptTableAddress());// Write new pc
    }

}
