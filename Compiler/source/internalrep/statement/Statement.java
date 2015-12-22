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
import internalrep.Node;
import internalrep.symbol.SymbolTable;
import internalrep.declaration.ClassDeclaration;

public abstract class Statement extends Node {

    public Statement(ClassDeclaration declaration, int lineNum) {
        super(declaration, lineNum);
    }

    public abstract void resolveType(SymbolTable st);

    public abstract void emit(Generator g);

    public abstract boolean returns();

}
