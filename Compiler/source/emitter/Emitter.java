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
package emitter;

import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.Data;
import internalrep.assembly.controlflow.Label;
import internalrep.constant.Literal;
import internalrep.declaration.Declaration;

import java.util.ArrayList;
import java.util.HashMap;

import main.Compiler;

public class Emitter {

    public static ArrayList<AssemblyStatement> emit(HashMap<String, Declaration> declarations,
            HashMap<String, Literal> literals, String startLabel) {
        ArrayList<AssemblyStatement> data = new ArrayList<AssemblyStatement>();
        Label end = new Label("");
        // Header
        emit(data, new Data("", end));// Size
        emitLong(data, 0);// Refcounter
        emitLong(data, 0);// Type (N/A)
        emitLong(data, 0);// Num Refs (0)
        emit(data, new Data("", "#" + startLabel));// Start offset
        final int lutSize = (int) Math.ceil((declarations.size() / 3.0) * 4.0);// Make the LUT big enough so its only 75% full
        emit(data, new Data("Declaration Lookup Table", lutSize));
        Label[] lookup = new Label[lutSize];
        for (Declaration d : declarations.values()) {
            long i = d.hash(lutSize);
            int index = (int) i;
            if (index != i) {
                Compiler.generatorError(d.fullName(), "Could not convert index to 32 bit");
            }
            boolean success = false;
            while (!success) {
                if (index < 0) {
                    index += lutSize;
                } else if (index >= lutSize) {
                    index -= lutSize;
                }
                if (lookup[index] == null) {
                    lookup[index] = d.mAddress;
                    success = true;
                }
                index++;
            }
        }
        for (Label l : lookup) {
            if (l == null) {
                emitLong(data, 0);
            } else {
                emit(data, new Data("", l));
            }
        }
        for (Declaration d : declarations.values()) {
            d.emit(data);
        }
        for (Literal l : literals.values()) {
            l.emit(data);
        }
        emit(data, end);
        return data;
    }

    public static void emitString(ArrayList<AssemblyStatement> data, String s) {
        emitLong(data, s.length());
        for (char c : s.toCharArray()) {
            emitLong(data, c);
        }
    }

    public static void emitLong(ArrayList<AssemblyStatement> data, long l) {
        emit(data, new Data("", l));
    }

    public static void emit(ArrayList<AssemblyStatement> data, AssemblyStatement s) {
        data.add(s);
    }
}
