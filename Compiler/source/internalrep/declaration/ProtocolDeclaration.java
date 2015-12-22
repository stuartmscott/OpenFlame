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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import emitter.Emitter;

import main.Compiler;

public class ProtocolDeclaration extends Declaration {

    private HashMap<String, Integer> mOffsets = new HashMap<String, Integer>();
    private HashMap<String, Declaration> mImplementers = new HashMap<String, Declaration>();
    private HashMap<String, Integer> mExtensions = new HashMap<String, Integer>();

    public ProtocolDeclaration(Compiler compiler, String module, String name) {
        super(compiler, module, name);
        // TODO Protocols should contain;
        // pointer to instance
        // list of offsets of the functions from the intance's pointer to class.
        // This is so that when a protocol is getting sent to another process,
        // only the instance's pointer to the class needs to be changed

        // FIXME when sending protocols between processes, they must have the same compilation version
        // of the protocol and implementing class or else the offsets/num functions may have changed
    }

    public void addExtension(String type, int lineNum) {
        mExtensions.put(type, lineNum);
    }

    public void addFunction(String name, int lineNum) {
        if (mOffsets.containsKey(name)) {
            Compiler.syntaxError(name, lineNum, name + " was already declared");
        }
        mOffsets.put(name, mOffset++);
    }

    public int getOffset(String function) {
        return mOffsets.get(function);
    }

    @Override
    public Set<String> getFunctions() {
        return mOffsets.keySet();
    }

    @Override
    public boolean hasFunction(String name) {
        return mOffsets.containsKey(name);
    }

    public void addImplementer(Declaration declaration, int lineNum) {
        if (!mImplementers.containsKey(declaration.fullName())) {
            mImplementers.put(declaration.fullName(), declaration);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProtocolDeclaration) {
            ProtocolDeclaration c = (ProtocolDeclaration) obj;
            return mModule.equals(c.mModule) && mName.equals(c.mName);
        }
        return false;
    }

    @Override
    public String fullString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name: ");
        sb.append(mModule);
        sb.append(".");
        sb.append(mName);
        sb.append("\nfunctions:");
        for (String f : mOffsets.keySet()) {
            sb.append("\n\t" + f);
        }
        return sb.toString() + "\n----------------------------";
    }

    @Override
    public void typeCheck() {
        for (String type : mExtensions.keySet()) {
            int lineNum = mExtensions.get(type);
            Declaration d = mCompiler.getDeclaration(type);
            if (!(d instanceof ProtocolDeclaration)) {
                Compiler.typeError(fullName(), lineNum, type + " is not a Protocol");
            }
            if (type.equals(fullName())) {
                Compiler.typeError(fullName(), lineNum, fullName() + " cannot extend itself");
            }
            for (String f : ((ProtocolDeclaration) d).mOffsets.keySet()) {
                addFunction(f, lineNum);
            }
        }

        for (Declaration declaration : mImplementers.values()) {
            // TODO must not include static functions
            for (String f : mOffsets.keySet()) {
                if (!declaration.hasFunction(f)) {
                    Compiler.typeError(declaration.fullName(), 0, declaration.fullName()
                            + " does not implement " + fullName() + ", missing " + f);
                }
            }
        }
    }

    @Override
    public void generate() {
        // TODO Auto-generated method stub

    }

    public void emit(ArrayList<AssemblyStatement> data) {
        super.emit(data);
        Emitter.emitLong(data, 1L);// Is a Protocol
    }

}
