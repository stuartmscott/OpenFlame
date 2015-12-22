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
import generator.RegisterMask;
import internalrep.assembly.controlflow.Jez;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.constant.Function;
import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;

import java.util.Collection;

import main.Compiler;

public class ReturnStatement extends Statement {

    public final Expression mExpression;
    public String mReturnType;
    public Label mEndOfFunction;
    private Collection<LocalVariable> mTopSymbols;

    public ReturnStatement(ClassDeclaration declaration, int lineNum, Expression expression, Function function) {
        super(declaration, lineNum);
        mExpression = expression;
        function.mReturnStatements.add(this);
    }

    @Override
    public void resolveType(SymbolTable st) {
        if (mExpression == null) {
            mReturnType = Type.VOID;
        } else {
            mReturnType = mExpression.getType(st);
            if (mReturnType.equals(Type.VOID)) {
                Compiler.typeError(mDeclaration.mName, mLineNum, "Expression has type void");
            }
        }
        // TODO all symbols
        mTopSymbols = st.allTopSymbols();
        for (LocalVariable v : mTopSymbols) {
            st.deleteSymbol(v);// delete all symbols on the top
        }
        // their values will be released at the end of the function
    }

    @Override
    public void emit(Generator g) {
        if (mExpression != null) {
            Register resultRegister = mExpression.retainIntoRegister(g);
            Register returnAddress = g.getExpressionRegister("ReturnStatement emit() returnAddress");
            RegisterMask registerMask = new RegisterMask(g, returnAddress);
            g.emitAssembly(new Pop("pop return address", registerMask));
            g.emitAssembly(new Push("push result", new RegisterMask(g, resultRegister)));
            g.emitAssembly(new Push("push return address", registerMask));
            g.freeExpressionRegister(returnAddress);
            g.freeVariableRegister(resultRegister, true);
        }
        // TODO this must free any variable used
        for (LocalVariable v : mTopSymbols) {
            v.unbindFromRegister(g, false);// release symbols regs
        }
        g.emitAssembly(new Jez("jump to endFunction", g.register_zero, mEndOfFunction.mName));
    }

    @Override
    public boolean returns() {
        return true;
    }

    @Override
    public String toString() {
        if (mExpression != null) {
            return "return " + mExpression;
        }
        return "return";
    }

}
