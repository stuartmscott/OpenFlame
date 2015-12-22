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
import internalrep.assembly.arithmeticlogic.Add;
import internalrep.assembly.datamovement.Load;
import internalrep.assembly.datamovement.Store;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.expression.Expression;
import internalrep.symbol.GlobalVariable;
import internalrep.symbol.SymbolTable;
import main.Compiler;

public class GlobalVariableReference extends VariableReference {

    public GlobalVariable mSymbol;
    public final Expression mExpression;
    private LocalVariableReference mLocalReference;

    public GlobalVariableReference(ClassDeclaration declaration, int lineNum, Expression expression, String name) {
        super(declaration, lineNum, name);
        mExpression = expression;
        if (expression instanceof LocalVariableReference) {
            mLocalReference = (LocalVariableReference) expression;
        }
    }

    public String resolveType(SymbolTable st) {
        String type = mExpression.getType(st);
        if (!mDeclaration.fullName().equals(type) && !Compiler.isCoreModule(mDeclaration.mModule)) {
            Compiler.syntaxError(mDeclaration.mName, mLineNum,
                    "Access to instance variables is only granted within the declaring type");
        }
        Declaration d = mDeclaration.mCompiler.getDeclaration(type);
        if (d instanceof ProtocolDeclaration) {
            Compiler.syntaxError(mDeclaration.mName, mLineNum, "Cannot access instance variables of a Protocol");
        }
        mSymbol = ((ClassDeclaration) d).getMember(d.fullName() + "." + mName);
        if (mSymbol == null) {
            Compiler.typeError(mDeclaration.mName, mLineNum, mName + " was not declared in " + type);
        }
        return mSymbol.getType();
    }

    public Register retainIntoRegister(Generator g) {
        Register addressRegister;
        boolean notLocalRef = mLocalReference == null;
        if (notLocalRef) {
            addressRegister = mExpression.retainIntoRegister(g);
        } else {
            // If its a local symbol we dont have to retain it, just read its value straight
            addressRegister = mLocalReference.mSymbol.getBoundRegister();
        }
        Register destinationRegister;
        if (mSymbol.hasVoidType()) {
            destinationRegister = g.getExpressionRegister("GlobalVariableReference.retainIntoRegister() destinationRegister");
            g.emitAssembly(new Load("load numRefs", addressRegister, ClassDeclaration.NUM_REFERENCES_OFFSET, destinationRegister));
            g.emitAssembly(new Add("skip references", false, addressRegister, destinationRegister, destinationRegister));
            g.emitAssembly(new Load("load variable", destinationRegister, mSymbol.getOffset() + ClassDeclaration.HEADER_SIZE, destinationRegister));
        } else {
            destinationRegister = g.getVariableRegister("GlobalVariableReference.retainIntoRegister() destinationRegister");
            g.emitAssembly(new Load("load variable", addressRegister, mSymbol.getOffset() + ClassDeclaration.HEADER_SIZE, destinationRegister));
            g.retainValueInRegister(destinationRegister);
        }
        if (notLocalRef) {// If its not a local symbol we have to release the value
            g.releaseValueInRegister(addressRegister);
            g.freeVariableRegister(addressRegister, true);
        }
        return destinationRegister;
    }

    public void saveValue(Generator g, Expression value) {
        Register newValue = value.retainIntoRegister(g);

        Register addressRegister;
        boolean notLocalRef = mLocalReference == null;
        if (notLocalRef) {
            addressRegister = mExpression.retainIntoRegister(g);
        } else {
            // If its a local symbol we dont have to retain it, just read its value straight
            addressRegister = mLocalReference.mSymbol.getBoundRegister();
        }

        if (mSymbol.hasVoidType()) {
            Register temporaryRegister = g.getExpressionRegister("GlobalVariableReference.saveValue() temporaryRegister");
            g.emitAssembly(new Load("load numReferences", addressRegister, ClassDeclaration.NUM_REFERENCES_OFFSET, temporaryRegister));
            g.emitAssembly(new Add("skip references", false, addressRegister, temporaryRegister, temporaryRegister));
            g.emitAssembly(new Store("store variable", temporaryRegister, mSymbol.getOffset() + ClassDeclaration.HEADER_SIZE, newValue));
            g.freeExpressionRegister(temporaryRegister);
            g.freeExpressionRegister(newValue);
        } else {
            // Load old value and release it
            Register oldValue = g.getVariableRegister("GlobalVariableReference.saveValue() oldValue");
            g.emitAssembly(new Load("load old variable", addressRegister, mSymbol.getOffset() + ClassDeclaration.HEADER_SIZE, oldValue));
            g.releaseValueInRegister(oldValue);
            g.freeVariableRegister(oldValue, true);

            // Store new value
            g.emitAssembly(new Store("store variable", addressRegister, mSymbol.getOffset() + ClassDeclaration.HEADER_SIZE, newValue));
            g.freeVariableRegister(newValue, true);
        }

        if (notLocalRef) {// If its not a local symbol we have to release the value
            g.releaseValueInRegister(addressRegister);
            g.freeVariableRegister(addressRegister, true);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof GlobalVariableReference) {
            if (mName.equals(((GlobalVariableReference) obj).mName)) {
                return mExpression.equals(((GlobalVariableReference) obj).mExpression);
            }
        }
        return false;
    }

    public String toString() {
        return mExpression + "." + mName;
    }
}
