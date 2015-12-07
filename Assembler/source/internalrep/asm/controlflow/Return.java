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

public class Return extends AsmStmt implements Emittable {

    private int addrRegIndex;

    public Return(int addrRegIndex, String comment) {
        super(comment);
        // sets the pc to the value in addrReg
        this.addrRegIndex = addrRegIndex;
    }

    public long emit() {
        // 0000 0010 reg
        return (2L << 56) | addrRegIndex;
    }

    public String toString() {
        return "ret r" + addrRegIndex + super.toString();
    }

}
