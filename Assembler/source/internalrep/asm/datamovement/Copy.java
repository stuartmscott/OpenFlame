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
package internalrep.asm.datamovement;

import internalrep.asm.arithmeticlogic.Add;

public class Copy extends Add {

    public Copy(int source, int destination, String comment) {
        super(false, source, 0, destination, comment);
        // Copies value in sourceRegister into destinationRegister
        if (destination < 2) {
            System.out.println("Copy does nothing: cannot write to r0 or r1");
        } else if (mSource1 == destination) {
            System.out.println("Copy does nothing: registers are the same");
        }
    }

}
