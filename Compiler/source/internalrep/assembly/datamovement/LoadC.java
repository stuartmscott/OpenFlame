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
import main.Compiler;

public class LoadC extends AssemblyStatement {

    private Register mDestination;
    private long mConstant;
    private int mDestinationIndex;
    private Label mLabel;
    private boolean mIsLabel = false;
    private String mName;

    public LoadC(String comment, long constant, Register destination) {
        super(comment);
        // Loads constant into destination
        mConstant = constant;
        mDestination = destination;
        mIsRegister = true;
    }

    public LoadC(String comment, long constant, int destinationIndex) {
        super(comment);
        mConstant = constant;
        mDestinationIndex = destinationIndex;
    }

    public LoadC(String comment, String name, Register destination) {
        super(comment);
        mName = name;
        mDestination = destination;
        mIsLabel = true;
        mIsRegister = true;
    }

    public LoadC(String comment, String name, int destinationIndex, boolean isLabel) {
        super(comment);
        mName = name;
        mDestinationIndex = destinationIndex;
        mIsLabel = isLabel;
    }

    public LoadC(String comment, Label label, Register destination) {
        super(comment);
        mDestination = destination;
        mLabel = label;
        mIsLabel = true;
        mIsRegister = true;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mDestinationIndex = mDestination.getHardwareRegister();
        }
    }

    private void toConstant() {
        if (mIsLabel) {
            mConstant = mLabel.getAbsolute();
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
                mConstant = l.getConstant(mName);
            }
        }
    }

    public long emit() {
        // 0001 mConstant dest
        toIndexies();
        toConstant();
        if (mConstant >= Math.pow(2, 48)) {
            Compiler.generatorError(null, "constant in loadc is greater than max value");
        } else if (mConstant < 0) {
            Compiler.generatorError(null, "constant in loadc is less than zero");
        }
        return (1L << 60L) | (mConstant << 12L) | (mDestinationIndex);
    }

    public String toString() {
        toIndexies();
        if (mName != null) {
            return "loadc " + mName + " r" + mDestinationIndex + super.toString();
        } if (mIsLabel) {
            return "loadc " + mLabel.mName + " r" + mDestinationIndex + super.toString();
        }
        return "loadc " + mConstant + " r" + mDestinationIndex + super.toString();
    }

}
