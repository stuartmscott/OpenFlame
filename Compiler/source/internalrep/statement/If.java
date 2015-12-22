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
import generator.Register;
import internalrep.assembly.controlflow.Jez;
import internalrep.assembly.controlflow.Label;
import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;
import internalrep.symbol.SymbolTable;

public class If extends ConditionalControlStatement {

    private StatementList mFalseStatement;

    public If(ClassDeclaration declaration, int lineNum, Expression condition, StatementList trueStatement, StatementList falseStatement) {
        super(declaration, lineNum, condition, trueStatement);
        mFalseStatement = falseStatement;
    }

    @Override
    public void resolveType(SymbolTable st) {
        super.resolveType(st);
        if (mFalseStatement != null) {
            st = new SymbolTable(st);
            mFalseStatement.resolveType(st);
            if (!returns()) {
                SymbolFree.addFrees(mDeclaration, mLineNum, mFalseStatement, st);
            }
        }
    }

    @Override
    public void emit(Generator g) {
        Register conditionRegister = mConditionReference.retainIntoRegister(g);
        Label endTrue = new Label("endIfTrue");
        mDeclaration.mCompiler.addLabel(endTrue, mDeclaration.fullName(), mLineNum);
        g.emitAssembly(new Jez("if false, jump to endIfTrue" + endTrue.mName, conditionRegister, endTrue.mName));
        g.freeExpressionRegister(conditionRegister);
        mStatement.emit(g);
        if (mFalseStatement != null) {
            Label endFalse = new Label("endIfFalse");
            mDeclaration.mCompiler.addLabel(endFalse, mDeclaration.fullName(), mLineNum);
            g.emitAssembly(new Jez("was true so skip false", g.register_zero, endFalse.mName));
            g.emitAssembly(endTrue);
            mFalseStatement.emit(g);
            g.emitAssembly(endFalse);
        } else {
            g.emitAssembly(endTrue);
        }
    }

    @Override
    public boolean returns() {
        return mStatement.returns() && (mFalseStatement == null || mFalseStatement.returns());
    }

    @Override
    public String toString() {
        return "if " + mCondition + " " + mStatement + ((mFalseStatement != null) ? " else " + mFalseStatement : " ");
    }

}
