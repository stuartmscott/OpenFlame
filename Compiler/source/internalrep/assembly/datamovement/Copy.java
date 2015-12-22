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
package internalrep.assembly.datamovement;

import generator.Register;
import internalrep.assembly.arithmeticlogic.Add;

public class Copy extends Add {

    public Copy(String comment, Register source, Register zero, Register destination) {
        // Copies value in source into destination
        super(comment, false, source, zero, destination);
    }

    public Copy(String comment, int sourceIndex, int destinationIndex) {
        super(comment, false, sourceIndex, 0, destinationIndex);
        if (sourceIndex == 0 && destinationIndex == 0) {
            System.out.println("All registers are 0 in Copy");
        }
    }

    public boolean shouldBeEmitted() {
        toIndexies();
        return (mSourceIndex1 != mDestinationIndex);
    }

}
