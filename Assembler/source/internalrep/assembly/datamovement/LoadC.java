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
import main.Assembler;

public class LoadC extends AssemblyStatement implements Emittable, Linkable {

    private static final double MAX_VALUE = Math.pow(2, 48);
    private long mConstant;
    private int mDestinationRegister;
    private Label mLabel;
    private boolean mIsLabel = false;
    private String mName;

    public LoadC(long constant, int destinationRegister, String comment) {
        super(comment);
        // Loads constant into destinationRegister
        mConstant = constant;
        mDestinationRegister = destinationRegister;
    }

    public LoadC(String name, int destinationRegister, boolean isLabel, String comment) {
        super(comment);
        mName = name;
        mDestinationRegister = destinationRegister;
        mIsLabel = isLabel;
    }

    private void resolve() {
        if (mIsLabel) {
            mConstant = mLabel.getAbsolute();
        }
    }

    public void link(Linker linker) {
        if (mName != null) {
            if (mIsLabel) {
                mLabel = linker.getLabel(mName);
            } else {
                mConstant = linker.getConstant(mName);
            }
        }
    }

    public long emit() {
        // 0001 constant destination
        resolve();
        if (mConstant >= MAX_VALUE) {
            Assembler.generatorError(null, "constant in loadc is greater than MAX_VALUE (2^48)");
        } else if (mConstant < 0) {
            Assembler.generatorError(null, "constant in loadc is less than zero");
        }
        return (1L << 60L) | (mConstant << 12L) | (mDestinationRegister);
    }

    public String toString() {
        resolve();
        if (mName != null) {
            return "loadc " + mName + " r" + mDestinationRegister + super.toString();
        }
        if (mIsLabel) {
            return "loadc " + mLabel.mName + " r" + mDestinationRegister + super.toString();
        }
        return "loadc " + mConstant + " r" + mDestinationRegister + super.toString();
    }

}
