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

import internalrep.asm.AsmStmt;
import io.Emittable;

public class Pop extends AsmStmt implements Emittable {

    private long mMask;

    public Pop(long mask, String comment) {
        super(comment);
        mMask = mask;
    }

    public long emit() {
        // 0110 mask
        return (6L << 60L) | mMask;
    }

    public String toString() {
        return "pop " + mMask + super.toString();
    }

}
