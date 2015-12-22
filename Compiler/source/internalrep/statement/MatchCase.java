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
import internalrep.assembly.arithmeticlogic.Add;
import internalrep.assembly.arithmeticlogic.Subtract;
import internalrep.assembly.controlflow.Jez;
import internalrep.assembly.controlflow.Jnz;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.Load;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.special.Command;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import main.Compiler;

public class MatchCase extends Statement {

    private final LocalVariable mVariable;
    private final StatementList mStatements;
    public Register mExpressionRegister;
    public String mVariableType;
    public Label mEndLabel;

    public MatchCase(ClassDeclaration declaration, int l, LocalVariable variable, StatementList statement) {
        super(declaration, l);
        mVariable = variable;
        mStatements = statement;
    }

    @Override
    public void resolveType(SymbolTable st) {
        st = new SymbolTable(st);
        if (mVariable.hasVoidType()) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "Cannot match to a void type");
        }
        st.addSymbol(mVariable, mDeclaration.mName, mLineNum);
        mVariableType = mVariable.getType();
        if (mDeclaration.mCompiler.getDeclaration(mVariableType) instanceof ProtocolDeclaration) {
            Compiler.typeError(mDeclaration.mName, mLineNum, "Cannot match to a protocol");
        }
        mStatements.resolveType(st);
        st.deleteSymbol(mVariable);// So that it isn't freed here, it gets freed in Match
        if (!mStatements.returns()) {
            SymbolFree.addFrees(mDeclaration, mLineNum, mStatements, st);
        }
    }

    @Override
    public void emit(Generator g) {
        // type
        Register typeRegister = g.getExpressionRegister("MatchCase.emit() typeRegister");
        Register temporaryRegister = g.getExpressionRegister("MatchCase.emit() temporaryRegister");
        g.emitAssembly(new Command("load code base", Command.GET_CODE_BASE, temporaryRegister));
        g.emitAssembly(new LoadC("load type address", '#' + mVariableType, typeRegister));
        g.emitAssembly(new Add("calc type address", false, temporaryRegister, typeRegister, temporaryRegister));
        g.freeExpressionRegister(temporaryRegister);

        // expression type
        Register argumentRegister = g.getExpressionRegister("MatchCase.emit() argumentRegister");
        g.emitAssembly(new Load("load expr type", mExpressionRegister, ClassDeclaration.TYPE_OFFSET, argumentRegister));

        Label nextLabel = new Label("next match case");
        mDeclaration.mCompiler.addLabel(nextLabel, mDeclaration.fullName(), mLineNum);
        // jump if they dont match, jump to next match case
        temporaryRegister = g.getExpressionRegister("MatchCase.emit() temporaryRegister");
        g.emitAssembly(new Subtract("test type match", false, argumentRegister, typeRegister, temporaryRegister));
        g.freeExpressionRegister(typeRegister);
        g.freeExpressionRegister(argumentRegister);
        g.emitAssembly(new Jnz("no match, jump to next match case", temporaryRegister, nextLabel.mName));
        g.freeExpressionRegister(temporaryRegister);
        mVariable.setBoundRegister(mExpressionRegister);// bind expression register
        mStatements.emit(g);
        g.emitAssembly(new Jez("jump to end of match", g.register_zero, mEndLabel.mName));
        g.emitAssembly(nextLabel);
    }

    @Override
    public boolean returns() {
        return mStatements.returns();
    }

    @Override
    public String toString() {
        return mVariable + " " + mStatements;
    }

}
