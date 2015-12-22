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
package internalrep.assembly.special;

import internalrep.assembly.AssemblyStatement;
import io.Emittable;

public abstract class Special extends AssemblyStatement implements Emittable {

    public static final int HALT = 0;
    public static final int SLEEP = 1;
    public static final int WAIT = 2;
    public static final int NOOP = 3;
    public static final int COMMAND = 4;
    public static final int SIGNAL = 5;
    public static final int INTERRUPT = 6;
    public static final int INTERRUPT_RETURN = 7;
    public static final int LOCK = 8;
    public static final int UNLOCK = 9;
    public static final int BREAK = 10;

    protected final long mType;

    public Special(long type, String comment) {
        super(comment);
        mType = type;
    }

    public long emit() {
        // 0000 01 type constant32 register1 register2
        return (1L << 58L) | (mType << 54L);
    }

}
