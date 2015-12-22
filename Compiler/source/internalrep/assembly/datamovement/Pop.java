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

import generator.RegisterMask;
import internalrep.assembly.AssemblyStatement;

public class Pop extends AssemblyStatement {

    private RegisterMask mRegisterMask;
    private long mMask;

    public Pop(String comment, RegisterMask registerMask) {
        super(comment);
        mRegisterMask = registerMask;
    }

    public boolean shouldBeEmitted() {
        toMask();
        return mMask != 0;
    }

    public long emit() {
        toMask();
        // 0110 mask
        return (6L << 60L) | mMask;
    }

    private void toMask() {
        mMask = mRegisterMask.getMask();
    }

    public String toString() {
        return "pop" + mRegisterMask + super.toString();
    }

}
