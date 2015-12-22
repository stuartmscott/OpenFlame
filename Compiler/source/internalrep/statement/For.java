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
import main.Compiler;
import internalrep.assembly.controlflow.Jez;
import internalrep.assembly.controlflow.Label;
import internalrep.expression.Expression;
import internalrep.symbol.SymbolTable;
import internalrep.declaration.ClassDeclaration;

public class For extends ConditionalControlStatement {

    private final Statement mInitial;
    private final Statement mChange;
    private StatementList mFinalFrees, mStatementFrees;

    public For(ClassDeclaration declaration, int lineNum, Statement initial, Expression condition, Statement change, StatementList body) {
        super(declaration, lineNum, condition, body);
        mInitial = initial;
        mChange = change;
        mFinalFrees = new StatementList(declaration, lineNum);
        mStatementFrees = new StatementList(declaration, lineNum);
    }

    @Override
    public void resolveType(SymbolTable st) {
        st = new SymbolTable(st);
        mInitial.resolveType(st);
        if (mInitial.returns()) {
            Compiler.semanticError(mDeclaration.fullName(), mLineNum, "return found in for loop initialize statement");
        }
        SymbolFree.addFrees(mDeclaration, mLineNum, mFinalFrees, st);
        st = new SymbolTable(st);
        mChange.resolveType(st);
        if (mChange.returns()) {
            Compiler.semanticError(mDeclaration.fullName(), mLineNum, "return found in for loop mChange statement");
        }
        super.resolveType(st);
        if (!mStatement.returns()) {
            SymbolFree.addFrees(mDeclaration, mLineNum, mStatementFrees, st);
        }
    }

    @Override
    public String toString() {
        return "for " + mInitial + " " + mCondition + " " + mChange + " " + mStatement;
    }

    @Override
    public void emit(Generator g) {
        mInitial.emit(g);
        Label start = new Label("startFor");
        mDeclaration.mCompiler.addLabel(start, mDeclaration.fullName(), mLineNum);
        Label end = new Label("endFor");
        mDeclaration.mCompiler.addLabel(end, mDeclaration.fullName(), mLineNum);
        g.emitAssembly(start);
        Register conditionRegister = mConditionReference.retainIntoRegister(g);
        g.emitAssembly(new Jez("if false, jump to endFor" + end.mName, conditionRegister, end.mName));
        g.freeExpressionRegister(conditionRegister);
        mStatement.emit(g);
        mChange.emit(g);
        mStatementFrees.emit(g);
        g.emitAssembly(new Jez("loop back to startFor", g.register_zero, start.mName));
        g.emitAssembly(end);
        mFinalFrees.emit(g);
    }

}
