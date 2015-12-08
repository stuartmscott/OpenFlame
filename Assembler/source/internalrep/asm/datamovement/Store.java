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
import internalrep.asm.controlflow.Label;
import io.Emittable;
import linker.Linkable;
import linker.Linker;

public class Store extends AsmStmt implements Emittable, Linkable {

    private long mOffset;
    private int mBaseRegister;
    private int mSourceRegister;
    private boolean mIsLabel = false;
    private Label mLabel;
    private String mName;

    public Store(int baseRegister, long offset, int sourceRegister, String comment) {
        super(comment);
        //Stores data in srcReg to memory[baseRegister+offset]
        mBaseRegister = baseRegister;
        mOffset = offset;
        mSourceRegister = sourceRegister;
    }

    public Store(int baseRegIndex, String name, int srcRegIndex, boolean isLabel, String comment) {
        super(comment);
        mBaseRegister = baseRegIndex;
        mName = name;
        mIsLabel = isLabel;
        mSourceRegister = srcRegIndex;
    }

    private void resolve() {
        if (mIsLabel && mLabel != null)
            mOffset = mLabel.getAbsolute();
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
        // 0101 offset base source
        resolve();
        return (5L << 60L) | (mOffset << 12L) | (mBaseRegister << 6L) | (mSourceRegister);
    }

    public String toString() {
        resolve();
        if (mName != null) {
            return "store r" + mBaseRegister + " " + mName + " r" + mSourceRegister + super.toString();
        }
        return "store r" + mBaseRegister + " " + mOffset + " r" + mSourceRegister + super.toString();
    }

}
