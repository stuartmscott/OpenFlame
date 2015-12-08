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
package internalrep.asm.controlflow;

import internalrep.asm.AsmStmt;
import io.Emittable;
import linker.Linkable;
import linker.Linker;

public abstract class Jump extends AsmStmt implements Emittable, Linkable {

    public static final int EZ = 0;
    public static final int NZ = 1;
    public static final int LZ = 2;
    public static final int LE = 3;

    protected long mConditionCode;
    protected int mConditionRegister;
    protected String mDestination;
    private Label label;

    public Jump(long conditionCode, int conditionRegister, String destination, String comment) {
        super(comment);
        // Note: as some numbers are floats other are ints, jumps only have
        // ez (z), nz (!z), lz (n), le (n|z) as numbers can easily be easily tested:
        //  z = all bits zero
        //  n = most significant bit (MSB) set
        // All jumps are relative, so use dest.address - address to get
        // relative offset, this must be small enough to store in the
        // instruction
        mConditionCode = conditionCode;
        mConditionRegister = conditionRegister;
        mDestination = destination;
    }

    public void link(Linker l) {
        if (mDestination != null) {
            label = l.getLabel(mDestination);
        }
    }

    public long emit() {
        // Format: 1 conditionCode backward address conditionRegister
        long destinationAddress = label.getRelative(mAddress);
        long backward = 0;
        if (destinationAddress < 0) {
            backward = 1;
            destinationAddress *= -1;
        }
        return (1L << 63L) | (mConditionCode << 61L) | (backward << 60L) | (destinationAddress << 12L) | (mConditionRegister);
    }
}
