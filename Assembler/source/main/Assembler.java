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
package main;

import internalrep.asm.AsmStmt;
import internalrep.asm.Data;
import internalrep.asm.controlflow.Label;
import io.Emittable;
import io.Writer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lexer.Lexer;
import linker.Linker;
import optimizer.Optimizer;
import parser.Parser;

public class Assembler {

    private final Map<String, Label> labels = new HashMap<String, Label>();
    private final Map<String, Data> constants = new HashMap<String, Data>();
    private AsmStmt mFirstStmt = null;
    private AsmStmt mCurrentStmt = null;

    private final String mMain;
    private final String mSourceDirectory;
    private final String mBinary;
    private final String mLog;
    private final Set<String> mFiles;

    private Assembler(String main, String sourceDirectory, String binary, String log, Set<String> files) {
        this.mMain = main;
        this.mSourceDirectory = sourceDirectory;
        this.mBinary = binary;
        this.mLog = log;
        this.mFiles = files;
    }

    private void run() {
        final File binFile = new File(mBinary);
        final File logFile = new File(mLog);
        include(mMain);
        // linker
        final Linker l = new Linker(constants, labels, mFirstStmt);
        l.link();
        // optimizer
        final Optimizer o = new Optimizer(mFirstStmt);
        List<Emittable> stmts = o.reduce();
        // writer
        final Writer w = new Writer(binFile, logFile, stmts);
        try {
            w.write();
        } catch (IOException e) {
            ioError(e);
        }
    }

    public void include(String file) {
        String fullpath = String.format("%s/%s", mSourceDirectory, file);
        if (!mFiles.contains(fullpath)) {
            error(String.format("%s was not in the file list", file));
        }
        // Lexem
        final Lexer l = new Lexer(fullpath);
        // parser
        final Parser p = new Parser(this, l, fullpath);
        p.parse();
    }

    public void addStatement(AsmStmt asm) {
        if (mFirstStmt == null) {
            mFirstStmt = asm;
        } else {
            mCurrentStmt.next = asm;
        }
        mCurrentStmt = asm;
    }

    public void addLabel(Label label, String filename, int lineNum) {
        if (labels.containsKey(label.name))
            syntaxError(filename, lineNum, label.name + " was already declared");
        labels.put(label.name, label);
    }

    public void addConstant(String name, Data constant, String filename, int lineNum) {
        if (constants.containsKey(name))
            syntaxError(filename, lineNum, name + " was already declared");
        constants.put(name, constant);
    }

    public static void main(String[] args) {
        String main = null;
        String sourceDirectory = null;
        String binary = null;
        String log = null;
        Set<String> files = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                main = args[++i];
            } else if (args[i].equals("-s")) {
                sourceDirectory = args[++i];
            } else if (args[i].equals("-b")) {
                binary = args[++i];
            } else if (args[i].equals("-l")) {
                log = args[++i];
            } else {
                files.add(args[i]);
            }
        }
        if (main == null || sourceDirectory == null || binary == null || log == null || files.size() == 0) {
            error("Usage:\nAssembler -m <main> -s <source-directory> -b <binary> -l <log> <files>");
        }
        final Assembler assembler = new Assembler(main, sourceDirectory, binary, log, files);
        assembler.run();
    }

    public static void syntaxError(String name, int lineNum, String error) {
        parseError("Syntax", name, lineNum, error);
    }

    public static void semanticError(String name, int lineNum, String error) {
        parseError("Semantic", name, lineNum, error);
    }

    public static void typeError(String name, int lineNum, String error) {
        parseError("Type", name, lineNum, error);
    }

    private static void parseError(String errorType, String name, int lineNum, String error) {
        error(errorType + " error in " + name + " at line " + lineNum + ": " + error);
    }

    public static void generatorError(String name, String error) {
        if (name != null) {
            error("error generating assembly for " + name + ": " + error);
        } else {
            error("error generating assembly: " + error);
        }
    }

    public static void linkError(String error) {
        error("error while linking: " + error);
    }

    public static void ioError(Exception e) {
        error("IO error: " + e);
    }

    public static void ioError(String error) {
        error("IO error: " + error);
    }

    public static void error(String error) {
        System.err.println(error);
        throw new RuntimeException();
    }

    public static void warn(String name, int lineNum, String warning) {
        System.err.println("Warning in " + name + " at line " + lineNum + ": " + warning);
    }
}
