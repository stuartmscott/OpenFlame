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
package linker;

import internalrep.asm.AsmStmt;
import internalrep.asm.Data;
import internalrep.asm.controlflow.Label;

import java.util.ArrayList;
import java.util.Map;

import main.Assembler;

public class Linker {

    private final Map<String, Data> constants;
    private final Map<String, Label> labels;
    private final AsmStmt stmt;

    public Linker(Map<String, Data> constants, Map<String, Label> labels, AsmStmt stmt) {
        this.constants = constants;
        this.labels = labels;
        this.stmt = stmt;
    }

    public void link() {
        AsmStmt s = stmt;
        while (s != null) {
            if (s instanceof Linkable) {
                ((Linkable) s).link(this);
            }
            s = s.next;
        }
    }

    public long getConstant(String name) {
        Data d = constants.get(name);
        if (d == null) {
            Assembler.linkError(name + " was not declared");
        }
        return d.value;
    }

    public Label getLabel(String name) {
        Label l = labels.get(name);
        if (l == null) {
            for (String key: labels.keySet()) {
                System.out.println(key + " : " + labels.get(key));
            }
            Assembler.linkError(name + " was not declared");
        }
        l.references++;
        return l;
    }
}
