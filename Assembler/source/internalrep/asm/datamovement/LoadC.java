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
import main.Assembler;

public class LoadC extends AsmStmt implements Emittable, Linkable {

    private long constant;
    private int destRegIndex;
    private Label label;
    private boolean isLabel = false;
    private String name;

    public LoadC(long constant, int destRegIndex, String comment) {
        super(comment);
        // Loads constant into destReg
        this.constant = constant;
        this.destRegIndex = destRegIndex;
    }

    public LoadC(String name, int destRegIndex, boolean isLabel, String comment) {
        super(comment);
        this.name = name;
        this.destRegIndex = destRegIndex;
        this.isLabel = isLabel;
    }

    private void resolve() {
        if (isLabel) {
            constant = label.getAbsolute();
        }
    }

    public void link(Linker linker) {
        if (name != null) {
            if (isLabel)
                label = linker.getLabel(name);
            else
                constant = linker.getConstant(name);
        }
    }

    public long emit() {
        // 0001 constant dest
        resolve();
        if (constant >= Math.pow(2, 48))
            Assembler.generatorError(null, "constant in loadc is greater than max value");
        else if (constant < 0)
            Assembler.generatorError(null, "constant in loadc is less than zero");
        return (1L << 60L) | (constant << 12L) | (destRegIndex);
    }

    public String toString() {
        resolve();
        if (name != null) {
            return "loadc " + name + " r" + destRegIndex + super.toString();
        }
        if (isLabel) {
            return "loadc " + label.name + " r" + destRegIndex + super.toString();
        }
        return "loadc " + constant + " r" + destRegIndex + super.toString();
    }

}
