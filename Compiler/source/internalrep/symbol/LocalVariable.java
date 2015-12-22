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
package internalrep.symbol;

import generator.Generator;
import generator.Register;
import internalrep.declaration.ClassDeclaration;
import main.Compiler;

public class LocalVariable extends Symbol {

    private Register mRegister = null;

    public LocalVariable(ClassDeclaration declaration, int lineNum, String name, String type) {
        super(declaration, lineNum, name);
        mType = type;
    }

    public LocalVariable(ClassDeclaration declaration, int lineNum, String name) {
        this(declaration, lineNum, name, null);
    }

    public String getType() {
        if (mType == null) {
            Compiler.syntaxError(mDeclaration.mName, mLineNum, "type of " + mName + " could not be inferred");
        }
        if (hasVoidType()) {
            if (Compiler.isCoreModule(mDeclaration.mModule)) {
                Compiler.warn(mDeclaration.mName, mLineNum, mName + " has a Void type");
            } else {
                Compiler.syntaxError(mDeclaration.mName, mLineNum, mName + " cannot have a Void type");
            }
        }
        return mType;
    }

    public Register bindToNewRegister(Generator g) {
        if (mRegister != null) {
            System.err.println("local variable bound mRegister should be null");
        }
        String str = "LocalVariable.bindToNewRegister() " + mName;
        if (hasVoidType()) {
            mRegister = g.getExpressionRegister(str);
        } else {
            mRegister = g.getVariableRegister(str);
        }
        return mRegister;
    }

    public void unbindFromRegister(Generator g, boolean release) {
        // NOTE: local variables with a void type do not use var regs
        if (hasVoidType()) {
            g.freeExpressionRegister(mRegister);
        } else {
            if (release) {
                g.releaseValueInRegister(mRegister);
            }
            g.freeVariableRegister(mRegister, release);
        }
        mRegister = null;
    }

    public Register getBoundRegister() {
        return mRegister;
    }

    public void setBoundRegister(Register register) {
        mRegister = register;
    }

}
