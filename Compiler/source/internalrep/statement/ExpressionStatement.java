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
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;
import main.Compiler;

public class ExpressionStatement extends Statement {

    public final Expression mExpression;

    public ExpressionStatement(ClassDeclaration declaration, int lineNum, Expression e) {
        super(declaration, lineNum);
        mExpression = e;
    }

    @Override
    public void resolveType(SymbolTable st) {
        // Otherwise stack is not popped off and object is not freed
        // TODO maybe have a delete for this
        if (!mExpression.getType(st).equals(Type.VOID)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "value of expression is not Void, therefore assign it to a variable");
        }
    }

    @Override
    public void emit(Generator g) {
        if (mExpression.retainIntoRegister(g) != null) {
            Compiler.generatorError(mDeclaration.fullName(), "expression statement did not release its register");
        }
    }

    @Override
    public boolean returns() {
        return false;
    }

    @Override
    public String toString() {
        return mExpression.toString();
    }

}
