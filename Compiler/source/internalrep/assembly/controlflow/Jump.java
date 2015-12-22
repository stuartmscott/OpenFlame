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
package internalrep.assembly.controlflow;

import generator.Register;
import internalrep.assembly.AssemblyStatement;
import linker.Linker;

public abstract class Jump extends AssemblyStatement {

    public static final int EZ = 0;
    public static final int NZ = 1;
    public static final int LZ = 2;
    public static final int LE = 3;

    protected long mType;
    protected Register mCondition;
    protected int mConditionIndex;
    protected String mDestination;
    private Label mLabel;

    public Jump(String comment, long type, Register condition, String destination) {
        super(comment);
        mType = type;
        mCondition = condition;
        mDestination = destination;
        mIsRegister = true;
        // Note: as some numbers are floats other are ints, jumps only have
        // ez (z), nz (!z), lz (n), le (n|z) as this can be easily tested:
        //  z = all bits zero
        //  n = MSB set
        // all jumps are relative, so use
        // mDestination.address - address to get
        // relative offset, this must be
        // small enough to store in the
        // instruction
    }

    public Jump(String comment, long type, int conditionIndex, String destination) {
        super(comment);
        mType = type;
        mConditionIndex = conditionIndex;
        mDestination = destination;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mConditionIndex = mCondition.getHardwareRegister();
        }
    }

    public boolean shouldBeEmitted() {
        return true;
    }

    public void link(Linker l) {
        if (mDestination != null) {
            mLabel = l.getLabel(mDestination);
        }
    }

    public long emit() {
        // 1 mType bkwd offset mCondition
        toIndexies();
        long destAddr = mLabel.getRelative(mAddress);
        long backward = 0;
        if (destAddr < 0) {
            backward = 1;
            destAddr*=-1;
        }
        return (1L << 63L) | (mType << 61L) | (backward << 60L) | (destAddr << 12L) | (mConditionIndex);
    }
}
