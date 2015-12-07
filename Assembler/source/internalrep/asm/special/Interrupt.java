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
package internalrep.asm.special;

import linker.Linkable;
import linker.Linker;

public class Interrupt extends Special implements Linkable {

    // Kernel Calls
    public static final int KERNEL_ADD = 8;
    public static final int KERNEL_EXIT = 9;
    public static final int KERNEL_RECEIVE = 10;
    public static final int KERNEL_SEND = 11;
    public static final int KERNEL_ALLOC = 12;
    public static final int KERNEL_FREE = 13;

    private long interruptId;
    private String constant;

    public Interrupt(long interruptId, String comment) {
        super(INTR, comment);
        this.interruptId = interruptId;
    }

    public Interrupt(String constant, String comment) {
        super(INTR, comment);
        this.constant = constant;
    }

    public void link(Linker linker) {
        if (constant != null) {
            interruptId = linker.getConstant(constant);
        }
    }

    public long emit() {
        return super.emit() | (interruptId << 18L);
    }

    public String toString() {
        if (constant != null) {
            return "intr " + constant + super.toString();
        }
        return "intr " + interruptId + super.toString();
    }

}
