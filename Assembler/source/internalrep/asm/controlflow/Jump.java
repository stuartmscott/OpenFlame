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

    protected long condCode;
    protected int condRegIndex;
    protected String dest;
    private Label label;

    public Jump(long condCode, int condRegIndex, String dest, String comment) {
        super(comment);
        // Note: as some numbers are floats other are ints, jumps only have
        // ez (z), nz (!z), lz (n), le (n|z) as numbers can easily be easily tested:
        //  z = all bits zero
        //  n = MSB set
        // All jumps are relative, so use dest.address - address to get
        // relative offset, this must be small enough to store in the
        // instruction
        this.condCode = condCode;
        this.condRegIndex = condRegIndex;
        this.dest = dest;
    }

    public void link(Linker l) {
        if (dest != null)
            label = l.getLabel(dest);
    }

    public long emit() {
        // 1 condCode bkwd offset condReg
        long destAddr = label.getRelative(address);
        long backward = 0;
        if (destAddr < 0) {
            backward = 1;
            destAddr*=-1;
        }
        return (1L << 63L) | (condCode << 61L) | (backward << 60L) | (destAddr << 12L) | (condRegIndex);
    }
}
