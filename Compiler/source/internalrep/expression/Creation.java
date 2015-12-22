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
package internalrep.expression;

import generator.Generator;
import generator.Register;
import generator.RegisterMask;
import internalrep.assembly.controlflow.Call;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.assembly.datamovement.Store;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.symbol.GlobalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;

import java.util.HashMap;
import java.util.HashSet;

import main.Compiler;

public class Creation extends Expression {

    private final String mType;
    private final HashMap<String, Expression> mInitializers;
    private ClassDeclaration mTarget;

    public Creation(ClassDeclaration declaration, int lineNum, String type, HashMap<String, Expression> initializers) {
        super(declaration, lineNum);
        mType = type;
        mInitializers = initializers;
    }

    public String resolveType(SymbolTable st) {
        Declaration d = mDeclaration.mCompiler.getDeclaration(mType);
        if (d instanceof ProtocolDeclaration) {
            Compiler.semanticError(mDeclaration.mName, mLineNum, "cannot instanciate a protocol type");
        }
        mTarget = (ClassDeclaration) d;
        // TODO maybe nice to be able to prevent people from instantiating a class
        /*
         * if (!mTarget.isExported("new")) {
         *     String err = "new was not exported by " + targetName;
         *     if (Compiler.isCoreModule(mDeclaration.mModule)) {
         *         Compiler.warn(mDeclaration.mName, mLineNum, err);
         *     } else {
         *         Compiler.typeError(mDeclaration.mName, mLineNum, err);
         *     }
         * }
         */

        HashMap<String, GlobalVariable> members = new HashMap<String, GlobalVariable>();
        members.putAll(mTarget.mVariables);
        // only allow assigning Void variables if module is lang/os
        if (Compiler.isCoreModule(mDeclaration.mModule)) {
            members.putAll(mTarget.mVoids);
        }
        for (String member : members.keySet()) {
            Expression e = mInitializers.get(member);
            if (e == null) {
                Compiler.semanticError(mDeclaration.mName, mLineNum, member + " was not initialized");
            }
            String expressionType = e.getType(st);
            String symbolType = members.get(member).getType();
            if (!expressionType.equals(symbolType)) {
                Compiler.typeError(mDeclaration.mName, mLineNum, expressionType + " cannot be assigned to "
                        + symbolType);
            }
        }
        if (mInitializers.size() > members.size()) {
            Compiler.semanticError(mDeclaration.mName, mLineNum, "more initializers than members");
        }
        return mType;
    }

    public Register retainIntoRegister(Generator g) {
        long numVariables = mTarget.mVariables.size();
        // variables + voids + header
        long instanceSize = numVariables + mTarget.mVoids.size() + ClassDeclaration.HEADER_SIZE;

        RegisterMask registersInUseMask = new RegisterMask(g, g.getRegistersInUse());
        g.emitAssembly(new Push("save used regs", registersInUseMask));

        Register destinationRegister = g.getVariableRegister("Creation.retainIntoRegister() destinationRegister");
        RegisterMask parametersMask = new RegisterMask(g, destinationRegister);

        // push return address
        Label returnLabel = new Label("return address");
        g.emitAssembly(new LoadC("load return address", returnLabel, destinationRegister));
        g.emitAssembly(new Push("push return address", new RegisterMask(g, destinationRegister)));

        g.emitAssembly(new LoadC("load mType address", "#" + mType, destinationRegister));
        g.emitAssembly(new Push("push mType param", parametersMask));

        g.emitAssembly(new LoadC("load size", instanceSize, destinationRegister));
        g.emitAssembly(new Push("push size param", parametersMask));

        g.emitAssembly(new LoadC("load address of create", "#lang.Core.create:lang.Void()", destinationRegister));
        g.emitAssembly(new Call("call create", destinationRegister));
        g.emitAssembly(returnLabel);

        g.emitAssembly(new Pop("pop result", new RegisterMask(g, destinationRegister)));

        g.emitAssembly(new Pop("restore used regs", registersInUseMask));

        // set num refs
        Register numVariablesRegister = g.getExpressionRegister("Creation.retainIntoRegister() numVariablesRegister");
        g.emitAssembly(new LoadC("load num variables", numVariables, numVariablesRegister));// load num variables
        g.emitAssembly(new Store("set num variables", destinationRegister, ClassDeclaration.NUM_REFERENCES_OFFSET, numVariablesRegister));
        g.freeExpressionRegister(numVariablesRegister);

        HashSet<String> names = new HashSet<String>();
        names.addAll(mTarget.mVariables.keySet());
        names.addAll(mTarget.mVoids.keySet());
        for (String name : names) {
            // Get value
            Expression initializer = mInitializers.get(name);
            Register expressionRegister = initializer.retainIntoRegister(g);
            int offset = mTarget.getMember(name).getOffset() + ClassDeclaration.HEADER_SIZE;
            if (initializer.mType.equals(Type.VOID)) {
                // Set symb
                g.emitAssembly(new Store("set variable", destinationRegister, offset + numVariables, expressionRegister));
                g.freeExpressionRegister(expressionRegister);
            } else {
                // Set symb
                g.emitAssembly(new Store("set variable", destinationRegister, offset, expressionRegister));
                g.freeVariableRegister(expressionRegister, true);
            }
        }

        return destinationRegister;
    }

    public String toString() {
        return "new " + mType + " {" + mInitializers + "}";
    }

}
