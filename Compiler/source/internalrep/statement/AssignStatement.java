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
package internalrep.statement;

import generator.Generator;
import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;
import internalrep.expression.reference.VariableReference;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;
import main.Compiler;

public class AssignStatement extends Statement {

    private final boolean mIsNewVariable;
    public final VariableReference mVariableReference;
    private final Expression mValue;

    public AssignStatement(ClassDeclaration declaration, int lineNum, boolean isNewVariable, VariableReference variableReference, Expression value) {
        super(declaration, lineNum);
        mIsNewVariable = isNewVariable;
        mVariableReference = variableReference;
        mValue = value;
    }

    @Override
    public void resolveType(SymbolTable st) {
        String expressionType = mValue.getType(st);
        if (expressionType.equals(Type.VOID) && !Compiler.isCoreModule(mDeclaration.mModule)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "Expression has type void");
        }
        if (mIsNewVariable) {
            LocalVariable variable = new LocalVariable(mDeclaration, mLineNum, mVariableReference.mName, expressionType);
            st.addSymbol(variable, mDeclaration.mName, mLineNum);
            mVariableReference.resolveType(st);
        } else {
            String symbolType = mVariableReference.getType(st);
            if (!expressionType.equals(symbolType)) {
                Compiler.typeError(mDeclaration.mName, mLineNum, expressionType + " cannot be assigned to " + symbolType);
            }
        }
    }

    @Override
    public void emit(Generator g) {
        // Save new mValue
        mVariableReference.saveValue(g, mValue);
    }

    @Override
    public boolean returns() {
        return false;
    }

    @Override
    public String toString() {
        return mVariableReference + " = " + mValue;
    }

}
