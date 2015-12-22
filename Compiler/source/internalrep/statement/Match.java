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
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.Load;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.expression.Expression;
import internalrep.symbol.SymbolTable;

import java.util.ArrayList;

import main.Compiler;

public class Match extends Statement {

    private final Expression mExpression;
    private final ArrayList<MatchCase> mCases;

    public Match(ClassDeclaration declaration, int lineNum, Expression expression, ArrayList<MatchCase> cases) {
        super(declaration, lineNum);
        mExpression = expression;
        mCases = cases;
    }

    public void resolveType(SymbolTable st) {
        Declaration d = mDeclaration.mCompiler.getDeclaration(mExpression.getType(st));
        if (!(d instanceof ProtocolDeclaration)) {
            Compiler.syntaxError(mDeclaration.mName, mLineNum,
                    "cannot match on an expression which is not a Protocol");
        }
        for (MatchCase c : mCases) {
            c.resolveType(st);
        }
    }

    public void emit(Generator g) {
        Register protocolRegister = mExpression.retainIntoRegister(g);
        Register valueRegister = g.getVariableRegister("Match.emit() valueRegister");
        g.emitAssembly(new Load("load inst", protocolRegister, 4, valueRegister));
        g.releaseValueInRegister(protocolRegister);
        g.freeVariableRegister(protocolRegister, true);
        g.retainValueInRegister(valueRegister);
        Label end = new Label("end of match");
        mDeclaration.mCompiler.addLabel(end, mDeclaration.fullName(), mLineNum);
        for (MatchCase c : mCases) {
            c.mExpressionRegister = valueRegister;
            c.mEndLabel = end;
            c.emit(g);
        }
        g.emitAssembly(end);
        g.releaseValueInRegister(valueRegister);
        g.freeVariableRegister(valueRegister, true);
    }

    @Override
    public boolean returns() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("match " + mExpression + " {");
        for (MatchCase c : mCases) {
            sb.append("\n" + c);
        }
        sb.append("\n}");
        return sb.toString();
    }

}
