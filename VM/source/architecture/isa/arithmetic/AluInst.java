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
package architecture.isa.arithmetic;

import architecture.IContext;
import architecture.isa.Instruction;

public abstract class AluInst extends Instruction {

    // http://www.eetimes.com/design/embedded/4212239/Hardware-Based-Floating-Point-Design-Flow-

    public static final int ADD = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int MOD = 4;
    public static final int CONVERT = 5;// Converts between integer and float

    public static final int AND = 6;
    public static final int OR = 7;
    public static final int XOR = 8;
    public static final int NOT = 9;

    public final int mSourceReg1;
    public final int mSourceReg2;
    public final int mDestinationReg;
    public final boolean mFloatingPoint;// is floating point operation

    public AluInst(IContext context, boolean floatingPoint, int sourceReg1,
            int sourceReg2, int destinationReg, String instructionName) {
        super(context, instructionName);
        mFloatingPoint = floatingPoint;
        mSourceReg1 = sourceReg1;
        mSourceReg2 = sourceReg2;
        mDestinationReg = destinationReg;
        // TODO design floating point representation or use ieee
        // TODO the loading, executing and formating of alu instructions needs
        // to change to floating point
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mSourceReg1));
        mPipeline.setLoadRegister1(mContext.getRegisterBank().read(mSourceReg2));
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
        // this will be used to saturate/format floating point numbers
    }

    public void store() {
        mContext.getRegisterBank().write(mDestinationReg, mPipeline.getFormatRegister0());
    }

}
