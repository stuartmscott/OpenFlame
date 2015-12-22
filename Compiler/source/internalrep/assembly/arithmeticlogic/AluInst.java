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
package internalrep.assembly.arithmeticlogic;

import generator.Register;
import internalrep.assembly.AssemblyStatement;

public abstract class AluInst extends AssemblyStatement {

    public static final int ADD = 0;
    public static final int SUBTRACT = 1;
    public static final int MULTIPLY = 2;
    public static final int DIVIDE = 3;
    public static final int MODULOS = 4;
    public static final int CONVERT = 5;//Converts between integer and float

    protected final boolean mIsFloat;
    protected final long mType;
    protected Register mSource1;
    protected Register mSource2;
    protected Register mDestination;

    protected int mSourceIndex1;
    protected int mSourceIndex2;
    protected int mDestinationIndex;

    public AluInst(String comment, boolean isFloat, long type, Register source1, Register source2, Register destination) {
        super(comment);
        mIsFloat = isFloat;
        mType = type;
        mSource1 = source1;
        mSource2 = source2;
        mDestination = destination;
        mIsRegister = true;
    }

    public AluInst(String comment, boolean isFloat, long type, int source1, int source2, int destination) {
        super(comment);
        mIsFloat = isFloat;
        mType = type;
        mSourceIndex1 = source1;
        mSourceIndex2 = source2;
        mDestinationIndex = destination;
    }

    public boolean shouldBeEmitted() {
        return true;
    }

    public long emit() {
        // 0000 1f type reg reg reg
        toIndexies();
        long flt = mIsFloat ? 1 : 0;
        return (1L << 59L) | (flt<< 58L) | (mType << 54L) | (mSourceIndex1 << 12L) | (mSourceIndex2 << 6L) | (mDestinationIndex);
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mSourceIndex1 = mSource1.getHardwareRegister();
            mSourceIndex2 = mSource2.getHardwareRegister();
            mDestinationIndex = mDestination.getHardwareRegister();
        }
    }
}
