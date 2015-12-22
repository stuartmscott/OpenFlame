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
import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.controlflow.Label;
import linker.Linker;

public class Store extends AssemblyStatement {

    private Register mSource;
    private Register mBase;
    private long mDisplacement;
    private int mBaseIndex;
    private int mSourceIndex;
    private boolean mIsLabel = false;
    private Label mLabel;
    private String mName;

    public Store(String comment, Register base, long displacement, Register source) {
        super(comment);
        // Stores data in source to mem[base+displacement]
        mBase = base;
        mDisplacement = displacement;
        mSource = source;
        mIsRegister = true;
    }

    public Store(String comment, int baseIndex, long displacement, int sourceIndex) {
        super(comment);
        mBaseIndex = baseIndex;
        mDisplacement = displacement;
        mSourceIndex = sourceIndex;
    }

    public Store(String comment, int baseIndex, String name, int sourceIndex, boolean isLabel) {
        super(comment);
        mBaseIndex = baseIndex;
        mName = name;
        mIsLabel = isLabel;
        mSourceIndex = sourceIndex;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mBaseIndex = mBase.getHardwareRegister();
            mSourceIndex = mSource.getHardwareRegister();
        }
        if (mIsLabel && mLabel != null) {
            mDisplacement = mLabel.getAbsolute();
        }
    }

    public boolean shouldBeEmitted() {
        return true;
    }

    public void link(Linker l) {
        if (mName != null) {
            if (mIsLabel) {
                mLabel = l.getLabel(mName);
            } else {
                mDisplacement = l.getConstant(mName);
            }
        }
    }

    public long emit() {
        // 0101 offset base destination
        toIndexies();
        return (5L << 60L) | (mDisplacement << 12L) | (mBaseIndex << 6L) | (mSourceIndex);
    }

    public String toString() {
        toIndexies();
        if (mName != null) {
            return "store r" + mBaseIndex + " " + mName + " r" + mSourceIndex + super.toString();
        }
        return "store r" + mBaseIndex + " " + mDisplacement + " r" + mSourceIndex + super.toString();
    }

}
