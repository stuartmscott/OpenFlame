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
package internalrep.asm.controlflow;

import internalrep.asm.AsmStmt;

public class Label extends AsmStmt {

    private static int curr_id = 0;
    public int references = 0;
    public String name;

    public Label(String comment) {
        this("#_lbl" + curr_id++, comment);
    }

    public Label(String name, String comment) {
        super(comment);
        this.name = name;
    }

    public String toString() {
        return name + super.toString();
    }

    public long getAbsolute() {
        return this.address;
    }

    public long getRelative(long address) {
        return this.address - address;
    }

}
