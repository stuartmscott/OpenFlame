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
package internalrep.constant;

import generator.Generator;
import generator.RegisterMask;
import generator.Register;
import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.controlflow.Call;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.controlflow.Return;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.assembly.special.Interrupt;
import internalrep.declaration.ClassDeclaration;
import internalrep.statement.ReturnStatement;
import internalrep.statement.Statement;
import internalrep.statement.StatementList;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;
import java.util.ArrayList;
import main.Compiler;

public class Function extends Constant {

    private static final int GENERAL_PURPOSE_REGISTER_COUNT = 64;//MAXIMUM OF 64

    public final String mReturnType;
    public final ArrayList<LocalVariable> mParameters;
    public final boolean mIsStatic;
    public StatementList mStatement;
    public ArrayList<AssemblyStatement> mInstructions = new ArrayList<AssemblyStatement>();
    public ArrayList<ReturnStatement> mReturnStatements = new ArrayList<ReturnStatement>();
    public boolean mIsNative = false;

    public Function(ClassDeclaration declaration, int lineNumber, String name, String returnType,
            ArrayList<LocalVariable> parameters, boolean isStatic) {
        super(declaration, lineNumber, fullName(name, returnType, parameters));
        mReturnType = returnType;
        mIsStatic = isStatic;
        mParameters = new ArrayList<LocalVariable>();
        if (!mIsStatic) {
            mParameters.add(new LocalVariable(mDeclaration, lineNumber, "this", mDeclaration.fullName()));
        }
        mParameters.addAll(parameters);
    }

    public void setStatement(Statement statement) {
        mStatement = new StatementList(mDeclaration, mLineNum);
        mStatement.add(statement);
    }

    public void typeCheck() {
        if (mIsNative) {
            return;
        }
        if (!mStatement.returns()) {
            if (mReturnType.equals(Type.VOID)) {
                mStatement.add(new ReturnStatement(mDeclaration, mLineNum, null, this));
            } else {
                String error = "no return statement found in function";
                if (Compiler.isCoreModule(mDeclaration.mModule)) {
                    Compiler.warn(mDeclaration.mName, mLineNum, error);
                } else {
                    Compiler.syntaxError(mDeclaration.mName, mLineNum, error);
                }
            }
        }

        SymbolTable list = new SymbolTable(null);
        for (LocalVariable s : mParameters) {
            list.addSymbol(s, mDeclaration.mName, mLineNum);
        }
        mStatement.resolveType(list);
        for (ReturnStatement r : mReturnStatements) {
            if (!r.mReturnType.equals(mReturnType)) {
                Compiler.syntaxError(mName, mLineNum, "type of return expression does not match return type in signature");
            }
        }
    }

    public void generate(Generator g) {
        if (!mIsNative) {
            // Create end of function label
            Label end = new Label("endFunction");
            mDeclaration.mCompiler.addLabel(end, mDeclaration.fullName(), mLineNum);
            for (ReturnStatement ret : mReturnStatements) {
                ret.mEndOfFunction = end;
            }

            // Pop parameters
            int numberParameters = mParameters.size();
            if (numberParameters > 0) {
                ArrayList<Register> parameterRegisters = new ArrayList<Register>(numberParameters);
                for (LocalVariable v : mParameters) {
                    parameterRegisters.add(v.bindToNewRegister(g));
                }
                g.emitAssembly(new Pop("pop parameters", new RegisterMask(g, parameterRegisters)));
            }

            mStatement.emit(g);

            g.emitAssembly(end);

            for (LocalVariable v : mParameters) {
                // Free the registers used by the mParameters
                v.unbindFromRegister(g, false);
            }

            g.allocateVariableRegisters();
            boolean[] wasUsed = new boolean[GENERAL_PURPOSE_REGISTER_COUNT];
            Register temporary = g.getExpressionRegister("Function.generate() temporary");
            for (Register r : g.mVariableRegisters) {
                // Free the set of variable registers
                int id = r.getHardwareRegister();
                if (!wasUsed[id]) {
                    Label l = new Label("");
                    g.emitAssembly(new LoadC("load return address", l, temporary));
                    g.emitAssembly(new Push("push return address", new RegisterMask(g, temporary)));
                    g.emitAssembly(new Push("push var to be released", new RegisterMask(g, r)));
                    g.emitAssembly(new LoadC("load address of release",
                                "#lang.Core.release:lang.Void()", temporary));
                    g.emitAssembly(new Call("call release", temporary));
                    g.emitAssembly(l);
                    wasUsed[id] = true;
                }
            }
            g.freeExpressionRegister(temporary);

            Register returnAddress = g.getExpressionRegister("Function.generate() returnAddress");
            g.emitAssembly(new Pop("pop return address", new RegisterMask(g, returnAddress)));
            g.emitAssembly(new Return("", returnAddress));
            g.freeExpressionRegister(returnAddress);

            g.allocateExpressionRegisters();
        }

    }

    public String toString() {
        return mName;
    }

    public static boolean isExported(String mDeclaration, String name) {
        return !name.startsWith("_") && !name.startsWith(mDeclaration + "._");
    }

    private static String fullName(String name, String returnType, ArrayList<LocalVariable> parameters) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(':');
        sb.append(returnType);
        sb.append('(');
        for (LocalVariable v : parameters) {
            sb.append(v.mType);
        }
        sb.append(')');
        return sb.toString();
    }

}
