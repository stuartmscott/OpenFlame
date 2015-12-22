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
import internalrep.assembly.arithmeticlogic.Convert;
import internalrep.assembly.datamovement.Load;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Store;
import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;
import main.Compiler;

public class GlobalOffsetReference extends VariableReference {

    private final Expression mExpression;
    private final Expression mOffset;
    private LocalVariableReference mLocalRef;

    public GlobalOffsetReference(ClassDeclaration declaration, int lineNum, Expression expression, Expression offset) {
        super(declaration, lineNum, "[" + offset + "]");
        // Allows access to an instance's local variables and voids
        mExpression = expression;
        mOffset = offset;
        if (expression instanceof LocalVariableReference) {
            mLocalRef = (LocalVariableReference) expression;
        }
    }

    public String resolveType(SymbolTable st) {
        if (mExpression.getType(st).equals(Type.VOID)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "expression must not be Void");
        }
        if (!mOffset.getType(st).equals(Type.VOID)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "offset expression must be Void");
        }
        return "#language.Anything";
    }

    public Register retainIntoRegister(Generator g) {
        Register offsetRegister = mOffset.retainIntoRegister(g);
        g.emitAssembly(new Convert("convert offset to int", true, offsetRegister, g.register_zero, offsetRegister));//Convert value from float to int
        Register temporaryRegister = g.getExpressionRegister("GlobalOffsetReference.retainIntoRegister() temporaryRegister");
        g.emitAssembly(new LoadC("load header size", ClassDeclaration.HEADER_SIZE, temporaryRegister));
        g.emitAssembly(new Add("add header to offset", false, offsetRegister, temporaryRegister, temporaryRegister));//add 4 to the offset (size, refcounter, type, numrefs)
        g.freeExpressionRegister(offsetRegister);

        Register addressRegister;
        if (mLocalRef != null) {
            //If its a local symbol we dont have to retain it, just read its value straight
            addressRegister = mLocalRef.mSymbol.getBoundRegister();
        } else {
            addressRegister = mExpression.retainIntoRegister(g);
        }
        g.emitAssembly(new Add("add offset to address", false, temporaryRegister, addressRegister, temporaryRegister));
        if (mLocalRef == null) {//If its not a local symbol we have to release the value
            g.releaseValueInRegister(addressRegister);
            g.freeVariableRegister(addressRegister, true);
        }

        Register destinationRegister = g.getVariableRegister("GlobalOffsetReference.retainIntoRegister() destinationRegister");
        g.emitAssembly(new Load("load value", temporaryRegister, 0, destinationRegister));
        g.retainValueInRegister(destinationRegister);
        g.freeExpressionRegister(temporaryRegister);
        return destinationRegister;
    }

    public void saveValue(Generator g, Expression value) {
        Register newValue = value.retainIntoRegister(g);

        Register offsetRegister = mOffset.retainIntoRegister(g);
        g.emitAssembly(new Convert("convert offset to int", true, offsetRegister, g.register_zero, offsetRegister));//Convert value from float to int
        Register temporaryRegister = g.getExpressionRegister("GlobalOffsetReference.saveValueInRegister() temporaryRegister");
        g.emitAssembly(new LoadC("load header size", ClassDeclaration.HEADER_SIZE, temporaryRegister));
        g.emitAssembly(new Add("add header to offset", false, offsetRegister, temporaryRegister, temporaryRegister));//add 4 to the offset (size, refcounter, type, numrefs)
        g.freeExpressionRegister(offsetRegister);

        Register addressRegister;
        if (mLocalRef != null) {
            //If its a local symbol we dont have to retain it, just read its value straight
            addressRegister = mLocalRef.mSymbol.getBoundRegister();
        } else {
            addressRegister = mExpression.retainIntoRegister(g);
        }
        g.emitAssembly(new Add("add offset to address", false, temporaryRegister, addressRegister, temporaryRegister));
        if (mLocalRef == null) {//If its not a local symbol we have to release the value
            g.releaseValueInRegister(addressRegister);
            g.freeVariableRegister(addressRegister, true);
        }

        //Load old value and release it
        Register oldValue = g.getVariableRegister("GlobalOffsetReference.saveValue() oldValue");
        g.emitAssembly(new Load("load old value", temporaryRegister, 0, oldValue));
        g.releaseValueInRegister(oldValue);
        g.freeVariableRegister(oldValue, true);

        //Store new value
        g.emitAssembly(new Store("store new value", temporaryRegister, 0, newValue));
        g.freeVariableRegister(newValue, true);
        g.freeExpressionRegister(temporaryRegister);
    }

    public String toString() {
        return mExpression + "." + mName;
    }

}
