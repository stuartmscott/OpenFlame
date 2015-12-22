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
package internalrep.expression;

import generator.Generator;
import generator.Register;
import generator.RegisterMask;
import internalrep.assembly.arithmeticlogic.Add;
import internalrep.assembly.controlflow.Call;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.Load;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.constant.Function;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.expression.reference.LocalVariableReference;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;

import java.util.ArrayList;

import main.Compiler;

public abstract class AbstractCall extends Expression {

    protected final ArrayList<Expression> mArgs;
    protected String mOwner;
    protected Declaration mOwnerDeclaration;
    protected Expression mExpression;
    protected String mName;
    private LocalVariableReference mLocalReference;
    private String mFunction;
    private final boolean mIsMethod;
    private boolean mIsCore;

    public AbstractCall(ClassDeclaration declaration, int lineNum, Expression expression, String name, ArrayList<Expression> args) {
        this(declaration, lineNum, name, args, true);
        mExpression = expression;
        if (expression instanceof LocalVariableReference) {
            mLocalReference = (LocalVariableReference) expression;
        }
    }

    public AbstractCall(ClassDeclaration declaration, int lineNum, String owner, String name, ArrayList<Expression> args) {
        this(declaration, lineNum, name, args, false);
        mOwner = owner;
    }

    private AbstractCall(ClassDeclaration declaration, int lineNum, String name, ArrayList<Expression> args, boolean isMethod) {
        super(declaration, lineNum);
        mName = name;
        mArgs = args;
        mIsMethod = isMethod;
        mIsCore = Compiler.isCoreModule(declaration.mModule);
    }

    @Override
    public String resolveType(SymbolTable st) {
        if (mIsMethod) {
            mOwner = mExpression.getType(st);
            mOwnerDeclaration = mDeclaration.mCompiler.getDeclaration(mOwner);
        } else {
            mOwnerDeclaration = mDeclaration.mCompiler.getDeclaration(mOwner);
            if (mOwnerDeclaration instanceof ProtocolDeclaration) {
                Compiler.typeError(mDeclaration.mName, mLineNum,
                        "Cannot invoke a Protocol's functions - no implementation");
            }
        }

        // TODO ensure cannot be Void unless lang/os
        StringBuilder sb = new StringBuilder("(");
        for (Expression e : mArgs) {
            String t = e.getType(st);
            if (t.equals(Type.VOID)) {
                Compiler.typeError(mDeclaration.mName, mLineNum, "Expression has type void");
            }
            sb.append(t);
        }
        sb.append(')');
        String given = sb.toString();

        String fullName = mOwnerDeclaration.fullName() + "." + mName;
        for (String f : mOwnerDeclaration.getFunctions()) {
            if (f.startsWith(fullName) && f.endsWith(given)) {
                mFunction = f;
            }
        }

        if (mFunction == null) {
            Compiler.typeError(mDeclaration.mName, mLineNum, fullName + given + " was not defined by "
                    + mOwnerDeclaration.mName);
        } else if (!mOwnerDeclaration.equals(mDeclaration) && !Function.isExported(mOwnerDeclaration.mName, mFunction)) {
            String err = mName + " was not exported by " + mOwnerDeclaration.mName;
            if (mIsCore) {
                Compiler.warn(mDeclaration.mName, mLineNum, err);
            } else {
                Compiler.typeError(mDeclaration.mName, mLineNum, err);
            }
        }

        return mFunction.substring(mFunction.indexOf(':') + 1, mFunction.indexOf('('));
    }

    @Override
    public Register retainIntoRegister(Generator g) {
        // save all registers used so far
        RegisterMask registersInUseMask = new RegisterMask(g, g.getRegistersInUse());
        g.emitAssembly(new Push("save used registers", registersInUseMask));

        Register functionRegister = g.getExpressionRegister("AbstractCall.retainIntoRegister() functionRegister");

        Label returnLabel = new Label("return address");
        g.emitAssembly(new LoadC("load return address", returnLabel, functionRegister));
        g.emitAssembly(new Push("push return address", new RegisterMask(g, functionRegister)));

        if (mIsMethod) {
            if (mOwnerDeclaration instanceof ProtocolDeclaration) {
                // TODO this needs re-doing
                Register expressionRegister;
                boolean notLocalRef = mLocalReference == null;
                if (notLocalRef) {
                    expressionRegister = mExpression.retainIntoRegister(g);
                } else {
                    // If its a local symbol we dont have to retain it, just read its value straight
                    expressionRegister = mLocalReference.mSymbol.getBoundRegister();
                }
                Register temporaryRegister = g.getExpressionRegister("AbstractCall.retainIntoRegister() temporaryRegister");
                g.emitAssembly(new LoadC("load offset", ((ProtocolDeclaration) mOwnerDeclaration).getOffset(mFunction), temporaryRegister));
                g.emitAssembly(new Add("add expression", false, expressionRegister, temporaryRegister, temporaryRegister));
                if (notLocalRef) {// If its not a local symbol we have to release the value
                    g.releaseValueInRegister(expressionRegister);
                    g.freeVariableRegister(expressionRegister, true);
                }
                g.emitAssembly(new Load("load func address", temporaryRegister, ClassDeclaration.HEADER_SIZE + 1, functionRegister));
                g.emitAssembly(new Load("load inst", temporaryRegister, ClassDeclaration.HEADER_SIZE, temporaryRegister));
                g.retainValueInRegister(temporaryRegister);
                g.emitAssembly(new Push("push inst", new RegisterMask(g, temporaryRegister)));
                g.freeExpressionRegister(temporaryRegister);
            } else {
                g.emitAssembly(new LoadC("load function address", "#" + mFunction, functionRegister));
                mExpression.retainOntoStack(g);
            }
        } else {
            g.emitAssembly(new LoadC("load function address", "#" + mFunction, functionRegister));
        }

        for (Expression e : mArgs) {
            e.retainOntoStack(g);
        }

        // calls will add the code base to the function address
        g.emitAssembly(new Call("", functionRegister));// Call the function
        g.freeExpressionRegister(functionRegister);// Free the address register
        g.emitAssembly(returnLabel);// Emit return address

        Register resultRegister = null;
        if (!mType.equals(Type.VOID)) {
            resultRegister = g.getVariableRegister("AbstractCall.emitCreate() resultRegister");
            g.emitAssembly(new Pop("pop result", new RegisterMask(g, resultRegister)));
            // No need to retain as the return has already done that
        }
        // Restore all registers used so far
        g.emitAssembly(new Pop("restore used registers", registersInUseMask));

        return resultRegister;
    }

    @Override
    public String toString() {
        return mExpression.toString() + "." + mName + "(" + mArgs + ")";
    }
}
