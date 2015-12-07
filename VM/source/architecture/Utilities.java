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
package architecture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Utilities {

    public static final boolean ARCH_EVENT = true;//Higher level events such as Halt

    //Context
    public static final boolean CONTEXT_STAGES = false;
    public static final boolean CONTEXT_INTERRUPT = true;
    public static final boolean INSTRUCTION_DECODED = true;

    public static final boolean REG_BANK = false;//Reads/writes to registers

    //Memory hierarchy
    public static final boolean CACHE_COMMAND = false;//When a read/write is issued to cache
    public static final boolean CACHE_HIERARCHY = false;//Traces events in cache hierarchy

    private static PrintWriter sTraceFile;

    static {
        try {
            sTraceFile = new PrintWriter(new File("build/trace.out"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void trace(boolean shouldTrace, String trace) {
        if (shouldTrace) {
            sTraceFile.println(trace);
        }
    }

    public static synchronized void flush() {
        sTraceFile.flush();
    }

    public static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }

}
