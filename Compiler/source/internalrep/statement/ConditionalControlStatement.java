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

import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;
import internalrep.expression.reference.GlobalVariableReference;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;
import main.Compiler;

public abstract class ConditionalControlStatement extends ControlStatement {

    protected Expression mCondition;
    protected GlobalVariableReference mConditionReference;

    public ConditionalControlStatement(ClassDeclaration declaration, int lineNum, Expression condition, StatementList statement) {
        super(declaration, lineNum, statement);
        mCondition = condition;
        mConditionReference = new GlobalVariableReference(declaration, lineNum, condition, "val");
    }

    @Override
    public void resolveType(SymbolTable st) {
        if (!mCondition.getType(st).equals(Type.NUMBER)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "condition statement must be of type language.Number");
        }
        if(!mConditionReference.getType(st).equals(Type.VOID)) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "expecting value of language.Number to be language.Void");
        }
        super.resolveType(st);
    }

}
