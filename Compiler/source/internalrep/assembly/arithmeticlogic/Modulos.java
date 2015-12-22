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

public class Modulos extends AluInst {

    public Modulos(String comment, boolean isFloat, Register source1, Register source2, Register destReg) {
        // Subtracts from source1 the value source2 and puts result in destReg
        // destReg = source1%source2
        super(comment, isFloat, MODULOS, source1, source2, destReg);
    }

    public Modulos(String comment, boolean isFloat, int source1, int source2, int destReg) {
        super(comment, isFloat, MODULOS, source1, source2, destReg);
    }

    public String toString() {
        toIndexies();
        return "mod r" + mSourceIndex1 + " r" + mSourceIndex2 + " r" + mDestinationIndex + super.toString();
    }

}
