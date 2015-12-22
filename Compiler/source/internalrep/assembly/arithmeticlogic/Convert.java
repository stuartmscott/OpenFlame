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

public class Convert extends AluInst {

    public Convert(String comment, boolean isFloat, Register source, Register zero, Register destination) {
        // Changes the value in source to float (if isFloat) or to an integer (if !isFloat)
        super(comment, isFloat, CONVERT, source, zero, destination);
    }

    public Convert(String comment, boolean isFloat, int source, int destination) {
        super(comment, isFloat, CONVERT, source, 0, destination);
    }

    public String toString() {
        toIndexies();
        return "convert r" + mSourceIndex1 + " r" + mDestinationIndex + super.toString();
    }

}
