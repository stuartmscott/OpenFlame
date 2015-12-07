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

import internalrep.asm.controlflow.Label;
import io.Emittable;
import linker.Linkable;
import linker.Linker;

public class Data extends AsmStmt implements Emittable, Linkable {

    public long value;
    public Label label;
    private String name;

    public Data(long value, String comment) {
        super(comment);
        this.value = value;
    }

    public Data(Label label, String comment) {
        super(comment);
        this.label = label;
    }

    public Data(String name, String comment) {
        super(comment);
        this.name = name;
    }

    public String toString() {
        if (label != null)
            return "data " + label.name + super.toString();
        return "data " + value + super.toString() + " // \'" + (char) (value + '\0') + "\' : "+ Long.toHexString(value) + " : " + Long.toBinaryString(value);
    }

    public void link(Linker l) {
        if (name != null) {
            label = l.getLabel(name);
        }
    }

    public long emit() {
        toValue();
        return value;
    }

    private void toValue() {
        if (label != null) {
            value = label.address;
        }
    }

    public boolean shouldBeEmitted() {
        return true;
    }

}
