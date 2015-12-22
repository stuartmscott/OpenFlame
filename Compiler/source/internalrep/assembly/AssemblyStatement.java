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
package internalrep.assembly;

import java.util.Arrays;

import linker.Linker;

public abstract class AssemblyStatement {

    // TODO ensure that bits don't overrun when emitting

    public long mAddress = -1;
    protected boolean mIsRegister = false;
    private final String mComment;

    public AssemblyStatement(String comment) {
        mComment = comment;
    }

    public void link(Linker l) {
    }

    public void setAddress(long address) {
        if (this.mAddress == -1) {
            this.mAddress = address;
        } else {
            System.err.println("Address already set");
        }
    }

    public abstract boolean shouldBeEmitted();

    public abstract long emit();

    public String toString() {
        if (mComment == null || mComment.equals("")) {
            return "";
        } else {
            return " // " + mComment;
        }
    }

    public static String padString(long value, int base, int length) {
        String s = Long.toString(value, base);
        StringBuilder sb = new StringBuilder(length);
        int len = length - s.length();
        if (len <= 0) {
            return s;
        }
        char[] zeros = new char[len];
        Arrays.fill(zeros, '0');
        sb.append(zeros);
        sb.append(s);
        return sb.toString();
    }
}
