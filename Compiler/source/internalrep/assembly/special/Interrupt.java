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

import linker.Linker;

public class Interrupt extends Special {

    // Kernel Calls
    public static final int KERNEL_ADD = 8;
    public static final int KERNEL_EXIT = 9;
    public static final int KERNEL_RECEIVE = 10;
    public static final int KERNEL_SEND = 11;
    public static final int KERNEL_ALLOCATE = 12;
    public static final int KERNEL_FREE = 13;

    private long mInterruptId;
    private String mConstant;

    public Interrupt(String comment, long interruptId) {
        super(comment, INTERRUPT);
        mInterruptId = interruptId;
    }

    public Interrupt(String comment, String constant) {
        super(comment, INTERRUPT);
        mConstant = constant;
    }

    public void link(Linker l) {
        if (mConstant != null) {
            mInterruptId = l.getConstant(mConstant);
        }
    }

    public long emit() {
        return super.emit() | (mInterruptId << 18L);
    }

    public String toString() {
        if (mConstant != null) {
            return "intr " + mConstant + super.toString();
        }
        return "intr " + mInterruptId + super.toString();
    }

}
