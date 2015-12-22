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

import internalrep.assembly.AssemblyStatement;
import io.Emittable;

public class Return extends AssemblyStatement implements Emittable {

    private int mAddressRegister;

    public Return(int addressRegister, String comment) {
        super(comment);
        // sets the pc to the value in addressRegister
        mAddressRegister = addressRegister;
    }

    public long emit() {
        // 0000 0010 register
        return (2L << 56) | mAddressRegister;
    }

    public String toString() {
        return "ret r" + mAddressRegister + super.toString();
    }

}
