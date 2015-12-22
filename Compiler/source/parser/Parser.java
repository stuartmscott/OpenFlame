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
package parser;

import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.constant.Function;
import internalrep.constant.Literal;
import internalrep.constant.NumberLiteral;
import internalrep.constant.StringLiteral;
import internalrep.expression.Creation;
import internalrep.expression.Expression;
import internalrep.expression.FunctionCall;
import internalrep.expression.MethodCall;
import internalrep.expression.ProtocolCast;
import internalrep.expression.reference.ConstantReference;
import internalrep.expression.reference.GlobalOffsetReference;
import internalrep.expression.reference.GlobalVariableReference;
import internalrep.expression.reference.LiteralReference;
import internalrep.expression.reference.LocalVariableReference;
import internalrep.expression.reference.VariableReference;
import internalrep.statement.AssignStatement;
import internalrep.statement.DoWhile;
import internalrep.statement.ExpressionStatement;
import internalrep.statement.For;
import internalrep.statement.If;
import internalrep.statement.Match;
import internalrep.statement.MatchCase;
import internalrep.statement.ReturnStatement;
import internalrep.statement.Statement;
import internalrep.statement.StatementList;
import internalrep.statement.While;
import internalrep.symbol.GlobalVariable;
import internalrep.symbol.LocalVariable;
import internalrep.type.Type;
import lexer.Category;
import lexer.Lexer;
import lexer.Token;
import main.Compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {

    private Declaration mDeclaration = null;
    private ClassDeclaration mClass = null;
    private ProtocolDeclaration mProtocol = null;
    private Function mFunction;

    // "Object" -> "lang.Object"
    public HashMap<String, String> mImportNames = new HashMap<String, String>();

    private final Compiler mCompiler;
    private final Lexer mLexer;
    private final File mFile;
    private final String mFilename;

    public Parser(Compiler compiler, Lexer lexer, File file) {
        mCompiler = compiler;
        mLexer = lexer;
        mFile = file;
        mFilename = file.getName();
        // Processes share code, so a class will have a reference counter, when that is zero, no
        // processes are using the class so it (and all its constants (literal & functions)) can be
        // removed from memory. As the code is shared, passing objects does not need its type
        // pointers to be changed (unless passing to external system)
    }

    private void move() {
        try {
            mLexer.move();
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private String match(Category c) {
        try {
            return mLexer.match(c);
        } catch (Exception e) {
            error(e.getMessage());
        }
        return "";
    }

    private void error(String string) {
        error(mLexer.getLineNumber(), string);
    }

    private void error(int lineNum, String string) {
        Compiler.syntaxError(mFilename, lineNum, string);
    }

    public Declaration parse() {
        move();
        // Imports
        while (mLexer.currentIs(Category.IMPORT)) {
            move();
            String moduleName = match(Category.LOWERNAME);
            match(Category.FULL_STOP);
            String typeName = match(Category.UPPERNAME);
            String importName;
            if (mLexer.currentIs(Category.AS)) {
                move();
                importName = match(Category.UPPERNAME);
            } else {
                importName = typeName;
            }
            mImportNames.put(importName, moduleName + "." + typeName);
        }

        // Class
        boolean isProtocol = mLexer.currentIs(Category.PROTOCOL);
        if (isProtocol) {
            move();
        } else {
            match(Category.CLASS);
        }

        for (String n : Compiler.CORE_NAMES) {
            mImportNames.put(n, "language." + n);
        }
        mImportNames.put("Void", Type.VOID);

        // Module
        String module = match(Category.LOWERNAME);
        match(Category.FULL_STOP);
        String name = match(Category.UPPERNAME);
        if (isProtocol) {
            mDeclaration = mProtocol = new ProtocolDeclaration(mCompiler, module, name);
        } else {
            mDeclaration = mClass = new ClassDeclaration(mCompiler, module, name);
        }

        match(Category.OCB);
        if (isProtocol) {
            while (!mLexer.currentIs(Category.CCB)) {
                int l = mLexer.getLineNumber();
                if (mLexer.currentIs(Category.UPPERNAME)) {
                    // <TypeName>
                    mProtocol.addExtension(matchType(), l);
                } else {
                    // <name>:<TypeName>(<opt-TypeName>)
                    StringBuilder sb = new StringBuilder(mDeclaration.fullName());
                    sb.append('.');
                    sb.append(mLexer.getCurrentValue());
                    match(Category.LOWERNAME);
                    match(Category.COLON);
                    sb.append(':');
                    sb.append(matchType());
                    sb.append('(');
                    match(Category.ORB);
                    while (!mLexer.currentIs(Category.CRB)) {
                        sb.append(matchType());
                    }
                    sb.append(')');
                    move();
                    mProtocol.addFunction(sb.toString(), l);
                }
            }
        } else {
            while (!mLexer.currentIs(Category.CCB)) {
                // Variable or Function
                int l = mLexer.getLineNumber();
                boolean isStatic = false;
                if (mLexer.currentIs(Category.UPPERNAME)) {
                    String value = mLexer.getCurrentValue();
                    if (!value.equals(name)) {
                        error("Expected \"" + name + "\", found \"" + value + "\" near:\n"
                                + mLexer.getSurrounding());
                    }
                    move();
                    match(Category.FULL_STOP);
                    isStatic = true;
                }
                String n = mDeclaration.fullName() + "." + mLexer.getCurrentValue();
                if (mLexer.currentIs(Category.UPPERNAME)) {
                    if (!isStatic) {
                        System.out.println("Constants must be declared as static");
                    }
                    move();
                    match(Category.COLON);
                    match(Category.ASSIGN);
                    mDeclaration.addConstant(n, matchLiteral(), l);
                } else {
                    match(Category.LOWERNAME);
                    match(Category.COLON);
                    String type = matchType();
                    if (mLexer.currentIs(Category.ORB)) {
                        mFunction = new Function(mClass, l, n, type, matchParameterTypes(), isStatic);
                        if (Compiler.isCoreModule(module) && mLexer.currentIs(Category.ASM)) {
                            mFunction.mIsNative = true;
                            move();
                        } else {
                            mFunction.setStatement(matchBlock());
                        }
                        mClass.addFunction(mFunction, l);
                    } else {
                        // <id>:<type>
                        GlobalVariable v = new GlobalVariable(mClass, l, n, type);
                        if (type.equals(Type.VOID)) {
                            mClass.addVoid(v, l);
                        } else {
                            mClass.addVariable(v, l);
                        }
                    }
                }
            }
        }
        return mDeclaration;
    }

    private LocalVariable matchVariable() {
        // <id>:<type>
        int l = mLexer.getLineNumber();
        String name = match(Category.LOWERNAME);
        match(Category.COLON);
        return new LocalVariable(mClass, l, name, matchType());
    }

    private String matchType() {
        String typeName = match(Category.UPPERNAME);
        if (typeName.equals("Void")) {
            return Type.VOID;
        }
        if (typeName.equals(mDeclaration.mName)) {
            return mDeclaration.fullName();
        }
        String fullName = mImportNames.get(typeName);
        if (fullName == null) {
            Compiler.typeError(mDeclaration.mName, mLexer.getLineNumber(), ": missing import: " + typeName);
        }
        return fullName;
    }

    private ArrayList<LocalVariable> matchParameterTypes() {
        match(Category.ORB);
        ArrayList<LocalVariable> params = new ArrayList<LocalVariable>();
        while (!mLexer.currentIs(Category.CRB)) {
            params.add(matchVariable());
        }
        move();
        return params;
    }

    private Statement matchStatement() {
        Expression e = null;
        StatementList b1, b2;
        int l = mLexer.getLineNumber();
        if (mLexer.currentIs(Category.LOWERNAME)) {
            e = matchExpression1();
            if (e instanceof VariableReference) {
                boolean isNewVar = false;
                if (mLexer.currentIs(Category.COLON) && (e instanceof LocalVariableReference)) {
                    move();
                    isNewVar = true;
                }
                match(Category.ASSIGN);
                return new AssignStatement(mClass, l, isNewVar, (VariableReference) e, matchExpression1());
            }
            // <expression>
            return new ExpressionStatement(mClass, l, e);
        } else if (mLexer.currentIs(Category.UPPERNAME)) {
            return new ExpressionStatement(mClass, l, matchExpression1());
        } else if (mLexer.currentIs(Category.RETURN)) {
            move();
            if (mLexer.currentIs(Category.ORB)) {
                e = matchExpression1();
                match(Category.CRB);
            }
            return new ReturnStatement(mClass, l, e, mFunction);
        } else if (mLexer.currentIs(Category.MATCH)) {
            move();
            e = matchExpression1();
            ArrayList<MatchCase> cases = new ArrayList<MatchCase>();
            match(Category.OCB);
            while (!mLexer.currentIs(Category.CCB)) {
                cases.add(new MatchCase(mClass, mLexer.getLineNumber(), matchVariable(), matchBlock()));
            }
            match(Category.CCB);
            return new Match(mClass, l, e, cases);
        } else if (mLexer.currentIs(Category.IF)) {
            move();
            e = matchExpression1();// condition
            b1 = matchBlock();// true block
            b2 = null;// false block
            if (mLexer.currentIs(Category.ELSE)) {
                move();
                b2 = matchBlock();
            }
            return new If(mClass, l, e, b1, b2);
        } else if (mLexer.currentIs(Category.DO)) {
            move();
            b1 = matchBlock();// body
            match(Category.WHILE);
            e = matchExpression1();// condition
            return new DoWhile(mClass, l, e, b1);
        } else if (mLexer.currentIs(Category.WHILE)) {
            move();
            e = matchExpression1();// condition
            b1 = matchBlock();// body
            return new While(mClass, l, e, b1);
        } else if (mLexer.currentIs(Category.FOR)) {
            move();
            Statement s1 = matchStatement();// initial
            e = matchExpression1();// condition
            Statement s2 = matchStatement();// change
            b1 = matchBlock();// body
            return new For(mClass, l, s1, e, s2, b1);
        }
        error("could not parse statement: " + mLexer.getCurrent());
        return null;
    }

    private StatementList matchBlock() {
        int l = mLexer.getLineNumber();
        match(Category.OCB);
        StatementList sl = new StatementList(mClass, l);
        while (!mLexer.currentIs(Category.CCB)) {
            sl.add(matchStatement());
        }
        move();
        return sl;
    }

    // <expression>.<variable> - GlobalVariableReference
    // <expression>.<function>(<args>) - MethodCall
    // <Type>.<function>(<args>) - FunctionCall
    // <Type>.<constant> - ConstantReference
    // <constant> - ConstantReference
    private Expression matchExpression1() {
        int l = mLexer.getLineNumber();
        Expression e;
        if (mLexer.currentIs(Category.UPPERNAME)) {
            // Function Call
            // <Type>.<function>(<args>)
            // <Type>.<constant>
            String t = matchType();
            match(Category.FULL_STOP);
            String name = mLexer.getCurrentValue();
            if (mLexer.currentIs(Category.UPPERNAME)) {
                move();
                e = new ConstantReference(mClass, l, t, name);
            } else {
                match(Category.LOWERNAME);
                e = new FunctionCall(mClass, l, t, name, matchArgs());
            }
        } else {
            e = matchExpression2();
        }
        while (mLexer.currentIs(Category.FULL_STOP)) {
            move();
            l = mLexer.getLineNumber();
            if (mLexer.currentIs(Category.OSB)) {
                if (!Compiler.isCoreModule(mDeclaration.mModule)) {
                    match(Category.LOWERNAME);
                }
                move();
                Expression offset = matchExpression1();
                match(Category.CSB);
                e = new GlobalOffsetReference(mClass, l, e, offset);
            } else {
                String name = match(Category.LOWERNAME);
                if (mLexer.currentIs(Category.ORB)) {
                    // Polymorphic Call
                    // <expression>.<name>(<args>)
                    e = new MethodCall(mClass, l, e, name, matchArgs());
                } else {
                    // Access
                    // <expression>.<variable> - where expression is a reference to an object of type mDeclaration
                    e = new GlobalVariableReference(mClass, l, e, name);
                }
            }
        }
        return e;
    }

    // (<Type> <expression>) - ProtocolCast
    // new <Type>{<initializers>} - Creation
    // <variable> - LocalVariableReference
    private Expression matchExpression2() {
        int l = mLexer.getLineNumber();
        String name, t;
        if (mLexer.currentIs(Category.ORB)) {
            move();
            t = matchType();
            Expression e = matchExpression1();
            match(Category.CRB);
            return new ProtocolCast(mClass, l, e, t);
        } else if (mLexer.currentIs(Category.NEW)) {
            move();
            t = matchType();
            match(Category.OCB);
            HashMap<String, Expression> initializers = new HashMap<String, Expression>();
            while (!mLexer.currentIs(Category.CCB)) {
                name = t + "." + match(Category.LOWERNAME);
                match(Category.ASSIGN);
                initializers.put(name, matchExpression1());
            }
            move();
            return new Creation(mClass, l, t, initializers);
        } else if (mLexer.currentIs(Category.LOWERNAME)) {
            name = mLexer.getCurrentValue();
            move();
            return new LocalVariableReference(mClass, l, name);
        } else if (mLexer.currentIs(Category.STRING_LITERAL) || mLexer.currentIs(Category.NUMBER_LITERAL) || mLexer.currentIs(Category.BOOLEAN_LITERAL)) {
            Literal literal = matchLiteral();
            String owner = mClass.fullName();
            mCompiler.addLiteral(literal, owner, mLexer.getLineNumber());
            return new LiteralReference(mClass, l, owner, literal.mName, literal.mType);
        }
        error("unrecognised expression: " + mLexer.getCurrent());
        return null;
    }

    private Literal matchLiteral() {
        int l = mLexer.getLineNumber();
        Token current = mLexer.getCurrent();
        move();
        String value = current.mValue;
        double d;
        String s;
        if (current.mCategory == Category.BOOLEAN_LITERAL) {
            d = (value.equals("true")) ? 1 : 0;
            return new NumberLiteral(mClass, l, d);
        } else if (current.mCategory == Category.NUMBER_LITERAL) {
            d = Double.parseDouble(value);
            return new NumberLiteral(mClass, l, d);
        } else if (current.mCategory == Category.STRING_LITERAL) {
            s = value.substring(1, value.length() - 1);
            return new StringLiteral(mClass, l, s);
        }
        error("Could not parse literal");
        return null;
    }

    private ArrayList<Expression> matchArgs() {
        match(Category.ORB);
        ArrayList<Expression> args = new ArrayList<Expression>();
        while (!mLexer.currentIs(Category.CRB)) {
            args.add(matchExpression1());
        }
        move();
        return args;
    }

}
