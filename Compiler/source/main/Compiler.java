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

import emitter.Emitter;
import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.Data;
import internalrep.assembly.controlflow.Label;
import internalrep.constant.Literal;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import io.Writer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lexer.Category;
import lexer.Lexem;
import lexer.Lexer;
import linker.Linker;
import optimizer.Optimizer;
import parser.Parser;

public class Compiler {

    public static final String[] CORE_NAMES = { "Anything", "Core", "Number", "Object", "String" };

    private final HashMap<String, Declaration> mDeclarations = new HashMap<String, Declaration>();
    private final HashMap<String, Label> mLabels = new HashMap<String, Label>();
    private final HashMap<String, Data> mConstants = new HashMap<String, Data>();
    private final HashMap<String, Literal> mLiterals = new HashMap<String, Literal>();
    private final ArrayList<String> mWaiting = new ArrayList<String>();

    private final String mMain;
    private final String mCoreDirectory;
    private final String mOutDirectory;
    private final Set<String> mFiles;

    private Compiler(String main, String coreDirectory, String outDirectory, Set<String> files) {
        mMain = main;
        mCoreDirectory = coreDirectory;
        mOutDirectory = outDirectory;
        mFiles = files;
        // Writes out each class to its oun assembly file, the assembler can then create a binary with all the code,
        // and with the first instruction being the main process's start function.
    }

    private void run() {
        for (String name : CORE_NAMES) {
            input(new File(String.format("%s/core/%s.flm", mCoreDirectory, name)));
        }
        for (String file : mFiles) {
            input(new File(file));
        }

        // type check
        for (Declaration d : mDeclarations.values()) {
            d.typeCheck();
        }
        // generate
        for (Declaration d : mDeclarations.values()) {
            d.generate();
        }
        /*
        // emit
        ArrayList<AssemblyStatement> asms = Emitter.emit(mDeclarations, getLiterals(), startLabel);
        // optimizer
        ArrayList<AssemblyStatement> statements = Optimizer.reduce(asms);
        // writer
        Writer.write(binFile, statements);
        */
    }

    private String input(File file) {
        try {
            String fullpath = file.getAbsolutePath();
            if (!mFiles.contains(fullpath)) {
                error(String.format("%s was not in the file list", file));
            }
            // Lexem
            final Lexer l = new Lexer(file);
            // parser
            final Parser p = new Parser(this, l, file);
            Declaration d = p.parse();
            String fullName = d.fullName();
            mDeclarations.put(fullName, d);
            return fullName;
        } catch (Exception e) {
            ioError(e);
        }
        return null;
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

    public void addLiteral(Literal literal, String filename, int lineNum) {
        mLabels.put(literal.mAddress.mName, literal.mAddress);
        mLiterals.put(literal.mName, literal);
    }

    public Declaration getDeclaration(String fullName) {
        return mDeclarations.get(fullName);
    }

    public HashMap<String, Literal> getLiterals() {
        return mLiterals;
    }

    public static boolean isCoreModule(String module) {
        return module.equals("lang") || module.equals("os");
    }

    public static void main(String[] args) {
        String main = null;
        String coreDirectory = null;
        String outDirectory = null;
        Set<String> files = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                main = args[++i];
            } else if (args[i].equals("-c")) {
                coreDirectory = args[++i];
            } else if (args[i].equals("-o")) {
                outDirectory = args[++i];
            } else {
                files.add(new File(args[i]).getAbsolutePath());
            }
        }
        if (main == null || coreDirectory == null || outDirectory == null || files.size() == 0) {
            error("Usage:\nCompiler -m <main> -c <coreDirectory> -o <outDirectory> <files>");
        }
        for (String file : files) {
            if (file.endsWith(main)) {
                // Convert main to a full file path
                main = file;
            }
        }
        final Compiler compiler = new Compiler(main, coreDirectory, outDirectory, files);
        compiler.run();
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
