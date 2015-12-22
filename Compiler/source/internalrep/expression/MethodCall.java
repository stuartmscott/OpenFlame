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

public class MethodCall extends AbstractCall {

    public MethodCall(ClassDeclaration declaration, int lineNum, Expression expression, String name, ArrayList<Expression> args) {
        super(declaration, lineNum, expression, name, args);
        // GlobalSymbolReg - expression.mName(args)
    }

}
