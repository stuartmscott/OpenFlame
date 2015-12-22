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

import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.controlflow.Label;
import io.Emittable;
import linker.Linkable;
import linker.Linker;

public class Load extends AssemblyStatement implements Emittable, Linkable {

    private long mOffset;
    private int mBaseRegister;
    private int mDestinationRegister;
    private boolean mIsLabel = false;
    private Label mLabel;
    private String mName;

    public Load(int baseRegister, long offset, int destinationRegister, String comment) {
        super(comment);
        // Loads into destinationRegister, the value at memory[baseRegister+offset]
        mBaseRegister = baseRegister;
        mOffset = offset;
        mDestinationRegister = destinationRegister;
    }

    public Load(int baseRegister, String name, int destinationRegister, boolean isLabel, String comment) {
        super(comment);
        mBaseRegister = baseRegister;
        mName = name;
        mDestinationRegister = destinationRegister;
        mIsLabel = isLabel;
    }

    protected void resolve() {
        if (mIsLabel) {
            mOffset = mLabel.getAbsolute();
        }
    }

    public void link(Linker linker) {
        if (mName != null) {
            if (mIsLabel) {
                mLabel = linker.getLabel(mName);
            } else {
                mOffset = linker.getConstant(mName);
            }
        }
    }

    public long emit() {
        // 0100 offset base destination
        resolve();
        return (4L << 60L) | (mOffset << 12L) | (mBaseRegister << 6L) | (mDestinationRegister);
    }

    public String toString() {
        resolve();
        if (mName != null) {
            return "load r" + mBaseRegister + " " + mName + " r" + mDestinationRegister + super.toString();
        }
        return "load r" + mBaseRegister + " " + mOffset + " r" + mDestinationRegister + super.toString();
    }

}
