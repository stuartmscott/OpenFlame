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

import io.Emittable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Writer {

    private final File mOutFile;
    private final File mLogFile;
    private final List<Emittable> mStatements;

    public Writer(File outFile, File logFile, List<Emittable> stmts) {
        mOutFile = outFile;
        mLogFile = logFile;
        mStatements = stmts;
    }

    public void write() throws IOException {
        if (!mOutFile.exists()) {
            mOutFile.createNewFile();
        }
        DataOutputStream out = new DataOutputStream(new FileOutputStream(mOutFile));
        PrintWriter log = new PrintWriter(mLogFile);
        // write instructions
        for (Emittable e : mStatements) {
            out.writeLong(e.emit());
            log.write(e.getAddress() + " : " + e + "\n");
        }
        out.close();
        log.close();
    }

}
