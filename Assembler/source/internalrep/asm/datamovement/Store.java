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

    private long displacement;
    private int baseRegIndex;
    private int srcRegIndex;
    private boolean isLabel = false;
    private Label label;
    private String name;

    public Store(int baseRegIndex, long displacement, int srcRegIndex, String comment) {
        super(comment);
        //Stores data in srcReg to mem[baseReg+displacement]
        this.baseRegIndex = baseRegIndex;
        this.displacement = displacement;
        this.srcRegIndex = srcRegIndex;
    }

    public Store(int baseRegIndex, String name, int srcRegIndex, boolean isLabel, String comment) {
        super(comment);
        this.baseRegIndex = baseRegIndex;
        this.name = name;
        this.isLabel = isLabel;
        this.srcRegIndex = srcRegIndex;
    }

    private void resolve() {
        if (isLabel && label != null)
            displacement = label.getAbsolute();
    }

    public void link(Linker linker) {
        if (name != null) {
            if (isLabel)
                label = linker.getLabel(name);
            else
                displacement = linker.getConstant(name);
        }
    }

    public long emit() {
        // 0101 offset base dest
        resolve();
        return (5L << 60L) | (displacement << 12L) | (baseRegIndex << 6L) | (srcRegIndex);
    }

    public String toString() {
        resolve();
        if (name != null) {
            return "store r" + baseRegIndex + " " + name + " r" + srcRegIndex + super.toString();
        }
        return "store r" + baseRegIndex + " " + displacement + " r" + srcRegIndex + super.toString();
    }

}
