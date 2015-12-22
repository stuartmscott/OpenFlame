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
package io;

import internalrep.assembly.AssemblyStatement;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import main.Compiler;

public class Writer {

    public static void write(File outFile, ArrayList<AssemblyStatement> instructions) {
        DataOutputStream out;
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            out = new DataOutputStream(new FileOutputStream(outFile));
            // write instructions
            for (AssemblyStatement s : instructions) {
                out.writeLong(s.emit());
            }
            out.close();
        } catch (Exception e) {
            Compiler.ioError(e);
        }
    }

    public static void log(File logFile, ArrayList<AssemblyStatement> instructions) {
        PrintWriter out;
        try {
            out = new PrintWriter(logFile);
            // log instructions
            for (AssemblyStatement s : instructions) {
                out.write(s.mAddress + " : " + s + "\n");
            }
            out.close();
        } catch (Exception e) {
            Compiler.ioError(e);
        }
    }

}
