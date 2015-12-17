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

    private final Map<String, Label> mLabels = new HashMap<String, Label>();
    private final Map<String, Data> mConstants = new HashMap<String, Data>();
    private AsmStmt mFirstStatement = null;
    private AsmStmt mCurrentStatement = null;

    private final String mMain;
    private final String mBinary;
    private final Set<String> mFiles;

    private Assembler(String main, String binary, Set<String> files) {
        mMain = main;
        mBinary = binary;
        mFiles = files;
    }

    private void run() {
        final File mainFile = new File(mMain);
        final String name = mainFile.getName().split("\\.")[0];
        final File binaryFile = new File(mBinary);
        binaryFile.getParentFile().mkdirs();
        final File logFile = new File(String.format("%s.log", binaryFile.getAbsolutePath()));
        include(mainFile);
        // linker
        final Linker l = new Linker(mConstants, mLabels, mFirstStatement);
        l.link();
        // optimizer
        final Optimizer o = new Optimizer(mFirstStatement);
        List<Emittable> stmts = o.reduce();
        // writer
        final Writer w = new Writer(binaryFile, logFile, stmts);
        try {
            w.write();
        } catch (IOException e) {
            ioError(e);
        }
    }

    public void include(File file) {
        String fullpath = file.getAbsolutePath();
        if (!mFiles.contains(fullpath)) {
            error(String.format("%s was not in the file list", file));
        }
        // Lexem
        final Lexer l = new Lexer(file);
        // parser
        final Parser p = new Parser(this, l, file);
        p.parse();
    }

    public void addStatement(AsmStmt asm) {
        if (mFirstStatement == null) {
            mFirstStatement = asm;
        } else {
            mCurrentStatement.mNext = asm;
        }
        mCurrentStatement = asm;
    }

    public void addLabel(Label label, String filename, int lineNum) {
        if (mLabels.containsKey(label.mName)) {
            syntaxError(filename, lineNum, label.mName + " was already declared");
        }
        mLabels.put(label.mName, label);
    }

    public void addConstant(String name, Data constant, String filename, int lineNum) {
        if (mConstants.containsKey(name)) {
            syntaxError(filename, lineNum, name + " was already declared");
        }
        mConstants.put(name, constant);
    }

    public static void main(String[] args) {
        String main = null;
        String binary = null;
        Set<String> files = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                main = args[++i];
            } else if (args[i].equals("-b")) {
                binary = args[++i];
            } else {
                files.add(new File(args[i]).getAbsolutePath());
            }
        }
        if (main == null || binary == null || files.size() == 0) {
            error("Usage:\nAssembler -m <main> -b <binary> <files>");
        }
        for (String file : files) {
            if (file.endsWith(main)) {
                // Convert main to a full file path
                main = file;
            }
        }
        final Assembler assembler = new Assembler(main, binary, files);
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
