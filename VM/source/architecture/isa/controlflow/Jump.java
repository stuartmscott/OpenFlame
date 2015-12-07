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
package architecture.isa.controlflow;

import architecture.IContext;
import architecture.isa.Instruction;

public class Jump extends Instruction {

    public static final int EZ = 0;// Equal to 0
    public static final int NZ = 1;// Not equal to 0
    public static final int LZ = 2;// Less than 0
    public static final int LE = 3;// Less than or equal to 0

    public static final String[] codes = {"ez", "nz", "lz", "le"};

    public final int mConditionCode;
    public final long mOffset;
    public final int mConditionRegister;

    public Jump(IContext context, boolean backward, int conditionCode, long offset, int freeRegister, int conditionRegister) {
        super(context, "Jump " + codes[conditionCode] + " " + (backward ? "b" : "f"));
        mConditionCode = conditionCode;
        mOffset = backward ? -offset : offset;
        mConditionRegister = conditionRegister;
        trace("Jump");
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mConditionRegister));
        mPipeline.setLoadRegister1(mContext.getRegisterBank().read(2));// Read old pc
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister1());

        // set context.incPC to false if branch is to be taken
        switch (mConditionCode) {
            case EZ:
                mIncrementProgramCounter = mPipeline.getLoadRegister0() != 0;
                break;
            case NZ:
                mIncrementProgramCounter = mPipeline.getLoadRegister0() == 0;
                break;
            case LZ:
                mIncrementProgramCounter = mPipeline.getLoadRegister0() >= 0;
                break;
            case LE:
                mIncrementProgramCounter = mPipeline.getLoadRegister0() > 0;
                break;
        }
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
    }

    public void store() {
        if (!mIncrementProgramCounter) {
            mContext.getRegisterBank().write(2, mPipeline.getFormatRegister0() + mOffset);//Write new pc
        }
    }

}
