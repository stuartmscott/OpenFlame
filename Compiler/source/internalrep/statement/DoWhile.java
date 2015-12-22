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
import internalrep.assembly.arithmeticlogic.Subtract;
import internalrep.assembly.controlflow.Jlz;
import internalrep.assembly.controlflow.Label;
import internalrep.declaration.ClassDeclaration;
import internalrep.expression.Expression;

public class DoWhile extends ConditionalControlStatement {

    public DoWhile(ClassDeclaration declaration, int lineNum, Expression condition, StatementList statement) {
        super(declaration, lineNum, condition, statement);
    }

    @Override
    public String toString() {
        return "do " + mStatement + " while " + mCondition;
    }

    @Override
    public void emit(Generator g) {
        Label start = new Label("startDoWhile");
        mDeclaration.mCompiler.addLabel(start, mDeclaration.fullName(), mLineNum);
        g.emitAssembly(start);
        mStatement.emit(g);
        Register conditionRegister = mConditionReference.retainIntoRegister(g);
        g.emitAssembly(new Subtract("test condition", true, g.register_zero, conditionRegister, conditionRegister));//invert it (>0 becomes <0 etc)
        g.emitAssembly(new Jlz("loop to startDoWhile", conditionRegister, start.mName));
        g.freeExpressionRegister(conditionRegister);
    }

}
