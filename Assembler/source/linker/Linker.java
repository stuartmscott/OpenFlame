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

import java.util.Map;

import main.Assembler;

public class Linker {

    private final Map<String, Data> mConstants;
    private final Map<String, Label> mLabels;
    private final AsmStmt mStatement;

    public Linker(Map<String, Data> constants, Map<String, Label> labels, AsmStmt statement) {
        this.mConstants = constants;
        this.mLabels = labels;
        this.mStatement = statement;
    }

    public void link() {
        AsmStmt statement = mStatement;
        while (statement != null) {
            if (statement instanceof Linkable) {
                ((Linkable) statement).link(this);
            }
            statement = statement.mNext;
        }
    }

    public long getConstant(String name) {
        Data data = mConstants.get(name);
        if (data == null) {
            Assembler.linkError(name + " was not declared");
        }
        return data.mValue;
    }

    public Label getLabel(String name) {
        Label label = mLabels.get(name);
        if (label == null) {
            for (String key: mLabels.keySet()) {
                System.out.println(key + " : " + mLabels.get(key));
            }
            Assembler.linkError(name + " was not declared");
        }
        label.mReferences++;
        return label;
    }
}
