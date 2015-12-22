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

import internalrep.assembly.AssemblyStatement;
import io.Emittable;

public abstract class AluInst extends AssemblyStatement implements Emittable {

    public static final int ADD = 0;
    public static final int SUB = 1;
    public static final int MULTIPLY = 2;
    public static final int DIVIDE = 3;
    public static final int MODULOS = 4;
    public static final int CONVERT = 5;//Converts between integer and float

    protected final boolean mIsFloat;
    protected final long mType;
    protected final int mSource1;
    protected final int mSource2;
    protected final int mDestination;

    public AluInst(boolean isFloat, long type, int source1, int source2, int destination, String comment) {
        super(comment);
        mIsFloat = isFloat;
        mType = type;
        mSource1 = source1;
        mSource2 = source2;
        mDestination = destination;
    }

    public long emit() {
        // 0000 1f type reg reg reg
        long flt = mIsFloat ? 1 : 0;
        return (1L << 59L) | (flt<< 58L) | (mType << 54L) | (mSource1 << 12L) | (mSource2 << 6L) | (mDestination);
    }

}
