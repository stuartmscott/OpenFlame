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

public class Load extends AsmStmt implements Emittable, Linkable {

    private long displacement;
    private int baseRegIndex;
    private int destRegIndex;
    private boolean isLabel = false;
    private Label label;
    private String name;

    public Load(int baseRegIndex, long displacement, int destRegIndex, String comment) {
        super(comment);
        // Loads into destReg, the value at mem[baseReg+displacement]
        this.baseRegIndex = baseRegIndex;
        this.displacement = displacement;
        this.destRegIndex = destRegIndex;
    }

    public Load(int baseRegIndex, String name, int destRegIndex, boolean isLabel, String comment) {
        super(comment);
        this.baseRegIndex = baseRegIndex;
        this.name = name;
        this.destRegIndex = destRegIndex;
        this.isLabel = isLabel;
    }

    protected void resolve() {
        if (isLabel)
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
        // 0100 offset base dest
        resolve();
        return (4L << 60L) | (displacement << 12L) | (baseRegIndex << 6L) | (destRegIndex);
    }

    public String toString() {
        resolve();
        if (name != null) {
            return "load r" + baseRegIndex + " " + name + " r" + destRegIndex + super.toString();
        }
        return "load r" + baseRegIndex + " " + displacement + " r" + destRegIndex + super.toString();
    }

}
