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
package internalrep.expression.reference;

import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.symbol.SymbolTable;
import main.Compiler;

public class ConstantReference extends LiteralReference {

    public ConstantReference(ClassDeclaration declaration, int lineNum, String owner, String name) {
        super(declaration, lineNum, owner, name, null);
    }

    public String resolveType(SymbolTable st) {
        Declaration d = mDeclaration.mCompiler.getDeclaration(mOwner);
        mLabel = d.mConstants.get(mOwner + "." + mName);
        if (mLabel == null) {
            Compiler.typeError(mDeclaration.mName, mLineNum, mName + " was not defined by " + mOwner);
        }
        return mType = mLabel.substring(1, mLabel.indexOf(':'));
    }

}
