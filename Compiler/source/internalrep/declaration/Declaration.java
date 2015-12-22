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

import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.controlflow.Label;
import internalrep.constant.Literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import main.Compiler;
import emitter.Emitter;

public abstract class Declaration {

    protected int mOffset = 0;

    public final Compiler mCompiler;
    public final String mModule;
    public final String mName;
    public final Label mAddress;

    public HashMap<String, String> mConstants = new HashMap<String, String>(); // "Foo.PI" ->
    // #lang.Number:3.14

    public Declaration(Compiler compiler, String module, String name) {
        mCompiler = compiler;
        mModule = module;
        mName = name;
        mAddress = new Label("", "#" + fullName());
        mCompiler.addLabel(mAddress, fullName(), 0);
    }

    public String toString() {
        return mName;
    }

    public String fullName() {
        return mModule + "." + mName;
    }

    public void addConstant(String name, Literal value, int lineNum) {
        if (mConstants.containsKey(name)) {
            Compiler.syntaxError(name, lineNum, name + " was already declared");
        }
        mConstants.put(name, value.mAddress.mName);
        mCompiler.addLiteral(value, fullName(), lineNum);
    }

    public abstract Set<String> getFunctions();

    public abstract boolean hasFunction(String name);

    public abstract String fullString();

    public abstract void typeCheck();

    public abstract void generate();

    public void emit(ArrayList<AssemblyStatement> data) {
        Emitter.emit(data, mAddress);
        Emitter.emitString(data, fullName());// full name
    }

    public long hash(long hashMapSize) {
        final String str = fullName();
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * 101 + str.charAt(i);
        }
        hash = hash % hashMapSize;
        // Java issue only
        if (hash < 0) {
            hash += hashMapSize;
        }
        return hash;
    }

}
