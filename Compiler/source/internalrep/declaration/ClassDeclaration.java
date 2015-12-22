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
package internalrep.declaration;

import emitter.Emitter;
import generator.Generator;
import internalrep.assembly.AssemblyStatement;
import internalrep.constant.Function;
import internalrep.symbol.GlobalVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import main.Compiler;

public class ClassDeclaration extends Declaration {

    public static final int SIZE_OFFSET = 0;
    public static final int REFERENCE_COUNTER_OFFSET = 1;
    public static final int TYPE_OFFSET = 2;
    public static final int NUM_REFERENCES_OFFSET = 3;
    public static final int REFERENCE_TABLE_OFFSET = 4;

    public static final int HEADER_SIZE = 4;// Defines how much header is at the top of an instance
    // (size, refcounter, type, numrefs)

    public HashMap<String, GlobalVariable> mVariables = new HashMap<String, GlobalVariable>();
    public HashMap<String, GlobalVariable> mVoids = new HashMap<String, GlobalVariable>();
    public HashMap<String, Function> mFunctions = new HashMap<String, Function>();

    public ClassDeclaration(Compiler mCompiler, String module, String name) {
        super(mCompiler, module, name);
    }

    public void addVariable(GlobalVariable variable, int lineNum) {
        if (mVariables.containsKey(variable.mName)) {
            Compiler.syntaxError(mName, lineNum, variable.mName + " was already declared");
        }
        if (mVoids.size() > 0) {
            Compiler.semanticError(mName, lineNum, "Voids must be declared after Variables");
        }
        mVariables.put(variable.mName, variable);
        variable.setOffset(mOffset++);
    }

    public void addVoid(GlobalVariable variable, int lineNum) {
        if (mVoids.containsKey(variable.mName)) {
            Compiler.syntaxError(mName, lineNum, variable.mName + " was already declared");
        }
        mVoids.put(variable.mName, variable);
        variable.setOffset(mOffset++);
    }

    public void addFunction(Function function, int lineNum) {
        if (mFunctions.containsKey(function.mName)) {
            Compiler.syntaxError(function.mName, lineNum, function.mName + " was already declared");
        }
        mFunctions.put(function.mName, function);
        if (!function.mName.startsWith("_")) {
            mCompiler.addLabel(function.mAddress, fullName(), lineNum);
        }
    }

    public Set<String> getFunctions() {
        return mFunctions.keySet();
    }

    public boolean hasFunction(String name) {
        return mFunctions.containsKey(name);
    }

    public GlobalVariable getMember(String name) {
        GlobalVariable symb = mVariables.get(name);
        if (symb == null) {
            symb = mVoids.get(name);
        }
        return symb;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ClassDeclaration) {
            ClassDeclaration c = (ClassDeclaration) obj;
            return mModule.equals(c.mModule) && mName.equals(c.mName);
        }
        return false;
    }

    public String fullString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name: ");
        sb.append(mModule);
        sb.append(".");
        sb.append(mName);
        sb.append("\nvariables:");
        for (GlobalVariable v : mVariables.values()) {
            sb.append("\n\t" + v.getOffset() + ": " + v);
        }
        for (GlobalVariable v : mVoids.values()) {
            sb.append("\n\t" + v.getOffset() + ": " + v);
        }
        sb.append("\nfunctions:");
        for (Function f : mFunctions.values()) {
            sb.append("\n\t" + f);
        }
        return sb.toString() + "\n----------------------------";
    }

    @Override
    public void typeCheck() {
        for (Function f : mFunctions.values()) {
            f.typeCheck();
        }
    }

    @Override
    public void generate() {
        for (Function f : mFunctions.values()) {
            f.generate(new Generator(f));
        }
    }

    @Override
    public void emit(ArrayList<AssemblyStatement> data) {
        super.emit(data);
        Emitter.emitLong(data, 0L);// Is not a Protocol
        // Write mFunctions
        Collection<Function> fns = mFunctions.values();
        Emitter.emitLong(data, fns.size());
        for (Function func : fns) {
            Emitter.emit(data, func.mAddress);
            for (AssemblyStatement s : func.mInstructions) {
                Emitter.emit(data, s);
            }
        }
    }

}
