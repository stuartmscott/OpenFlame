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
import internalrep.Node;
import internalrep.assembly.datamovement.Push;
import internalrep.declaration.ClassDeclaration;
import internalrep.symbol.SymbolTable;
import internalrep.type.Type;

public abstract class Expression extends Node {

    public Expression(ClassDeclaration declaration, int lineNum) {
        super(declaration, lineNum);
    }

    public String getType(SymbolTable st) {
        if (mType == null || mType.equals("")) {
            mType = resolveType(st);
        }
        return mType;
    }

    public abstract String resolveType(SymbolTable st);

    public abstract Register retainIntoRegister(Generator g);

    public void retainOntoStack(Generator g) {
        Register destinationRegister = retainIntoRegister(g);
        g.emitAssembly(new Push("", new RegisterMask(g, destinationRegister)));
        if (mType.equals(Type.VOID)) {
            g.freeExpressionRegister(destinationRegister);
        } else {
            g.freeVariableRegister(destinationRegister, true);
        }
    }

}
