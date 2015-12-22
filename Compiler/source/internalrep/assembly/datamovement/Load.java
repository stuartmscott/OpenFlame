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

public class Load extends AssemblyStatement {

    private Register mDestination;
    private Register mBase;
    private long mDisplacement;
    private int mBaseIndex;
    private int mDestinationIndex;
    private boolean mIsLabel = false;
    private Label mLabel;
    private String mName;

    public Load(String comment, Register base, long displacement, Register destination) {
        super(comment);
        // Loads into destination, the value at mem[base+displacement]
        mBase = base;
        mDisplacement = displacement;
        mDestination = destination;
        mIsRegister = true;
    }

    public Load(String comment, int baseIndex, long displacement, int destinationIndex) {
        super(comment);
        mBaseIndex = baseIndex;
        mDisplacement = displacement;
        mDestinationIndex = destinationIndex;
    }

    public Load(String comment, int mBaseIndex, String mName, int mDestinationIndex, boolean mIsLabel) {
        super(comment);
        mBaseIndex = mBaseIndex;
        mName = mName;
        mDestinationIndex = mDestinationIndex;
        mIsLabel = mIsLabel;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mBaseIndex = mBase.getHardwareRegister();
            mDestinationIndex = mDestination.getHardwareRegister();
        }
        if (mIsLabel) {
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
        // 0100 offset base destination
        toIndexies();
        return (4L << 60L) | (mDisplacement << 12L) | (mBaseIndex << 6L) | (mDestinationIndex);
    }

    public String toString() {
        toIndexies();
        if (mName != null) {
            return "load r" + mBaseIndex + " " + mName + " r" + mDestinationIndex + super.toString();
        }
        return "load r" + mBaseIndex + " " + mDisplacement + " r" + mDestinationIndex + super.toString();
    }

}
