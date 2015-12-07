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
package internalrep.asm;

import io.Addressable;
import java.util.Arrays;
import main.Assembler;

public abstract class AsmStmt implements Addressable {

    // TODO ensure that bits don't overrun when emitting

    public final String comment;
    public long address = -1;
    public AsmStmt next;

    public AsmStmt(String comment) {
        this.comment = comment;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public long getAddress() {
        return address;
    }

    public String toString() {
        if (comment == null || comment.equals(""))
            return "";
        else
            return " //" + comment;
    }

    public static String padStr(long value, int base, int length) {
        String s = Long.toString(value, base);
        StringBuilder sb = new StringBuilder(length);
        int len = length - s.length();
        if (len <= 0)
            return s;
        char[] zeros = new char[len];
        Arrays.fill(zeros, '0');
        sb.append(zeros);
        sb.append(s);
        return sb.toString();
    }
}
