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
package internalrep.asm.arithmeticlogic;

public class Multiply extends AluInst {

    public Multiply(boolean isFloat, int source1, int source2, int destination, String comment) {
        // Multiply source1 by source2 and put result in destination
        // destination = source1*source2
        super(isFloat, MULTIPLY, source1, source2, destination, comment);
    }

    public String toString() {
        return "mul r" + mSource1 + " r" + mSource2 + " r" + mDestination + super.toString();
    }

}
