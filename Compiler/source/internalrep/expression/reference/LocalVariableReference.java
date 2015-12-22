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
package internalrep.expression.reference;

import generator.Generator;
import generator.Register;
import generator.RegisterMask;
import internalrep.assembly.datamovement.Copy;
import internalrep.assembly.datamovement.Push;
import internalrep.expression.Expression;
import main.Compiler;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.declaration.ClassDeclaration;

public class LocalVariableReference extends VariableReference {

    public LocalVariable mSymbol;

    public LocalVariableReference(ClassDeclaration declaration, int lineNum, String name) {
        super(declaration, lineNum, name);
    }

    public LocalVariableReference(ClassDeclaration declaration, int lineNum, LocalVariable symbol) {
        super(declaration, lineNum, symbol.mName);
        mSymbol = symbol;
    }

    public String resolveType(SymbolTable st) {
        if (mSymbol == null) {
            mSymbol = st.getSymbol(mName);
            if (mSymbol == null) {
                Compiler.syntaxError(mDeclaration.mName, mLineNum, mName + " was not declared in this scope");
            }
        }
        return mSymbol.getType();
    }

    public Register retainIntoRegister(Generator g) {
        Register destinationRegister;
        if (mSymbol.hasVoidType()) {// Do not retain void
            destinationRegister = g.getExpressionRegister("LocalVariableReference.retainIntoRegister() destinationRegister");
            g.emitAssembly(new Copy("copy variable", mSymbol.getBoundRegister(), g.register_zero, destinationRegister));
        } else {
            destinationRegister = g.getVariableRegister("LocalVariableReference.retainIntoRegister() destinationRegister");
            g.emitAssembly(new Copy("copy variable", mSymbol.getBoundRegister(), g.register_zero, destinationRegister));
            g.retainValueInRegister(destinationRegister);
        }
        return destinationRegister;
    }

    public void retainOntoStack(Generator g) {
        Register register = mSymbol.getBoundRegister();
        if (!mSymbol.hasVoidType()) {// Do not retain void
            g.retainValueInRegister(register);
        }
        g.emitAssembly(new Push("push variable", new RegisterMask(g, register)));
    }

    public void saveValue(Generator g, Expression value) {
        // Store new value
        Register expressionRegister = value.retainIntoRegister(g);

        // Load old value and release it
        Register register = mSymbol.getBoundRegister();
        if (register == null) {
            register = mSymbol.bindToNewRegister(g);
        } else if (!mSymbol.hasVoidType()) {
            g.releaseValueInRegister(register);
        }
        g.emitAssembly(new Copy("copy variable", expressionRegister, g.register_zero, register));
        if (mSymbol.hasVoidType()) {
            g.freeExpressionRegister(expressionRegister);
        } else {
            g.freeVariableRegister(expressionRegister, true);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof LocalVariableReference) {
            return mName.equals(((LocalVariableReference) obj).mName);
        }
        return false;
    }

    public String toString() {
        return mName;
    }
}
