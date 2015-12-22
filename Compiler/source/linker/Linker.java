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

import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.Data;
import internalrep.assembly.controlflow.Label;

import java.util.ArrayList;
import java.util.HashMap;

import main.Compiler;

public class Linker {

    private final HashMap<String, Data> constants;
    private final HashMap<String, Label> labels;

    public Linker(HashMap<String, Data> constants, HashMap<String, Label> labels) {
        this.constants = constants;
        this.labels = labels;
    }

    public void link(ArrayList<AssemblyStatement> ss) {
        for (AssemblyStatement s : ss) {
            s.link(this);
        }
    }

    public long getConstant(String name) {
        Data d = constants.get(name);
        if (d == null) {
            Compiler.linkError(name + " was not declared");
        }
        return d.mValue;
    }

    public Label getLabel(String name) {
        Label l = labels.get(name);
        if (l == null) {
            for (String key: labels.keySet()) {
                System.out.println(key + " : " + labels.get(key));
            }
            Compiler.linkError(name + " was not declared");
        }
        l.mReferences++;
        return l;
    }
}
