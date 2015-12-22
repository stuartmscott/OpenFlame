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

import internalrep.declaration.ClassDeclaration;

import java.util.ArrayList;

public class FunctionCall extends AbstractCall {

    public FunctionCall(ClassDeclaration declaration, int lineNum, String owner, String name, ArrayList<Expression> args) {
        super(declaration, lineNum, owner, name, args);
        // GlobalSymbolReg - TypeRef.mName(args)
    }

    public boolean equals(Object obj) {
        if (obj instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) obj;
            return mExpression.equals(f.mExpression) && mName.equals(f.mName);
        }
        return false;
    }

}
