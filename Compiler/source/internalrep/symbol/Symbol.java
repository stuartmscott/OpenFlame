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

import internalrep.type.Type;
import internalrep.declaration.ClassDeclaration;

public abstract class Symbol {

    protected final ClassDeclaration mDeclaration;
    public final String mName;
    public final int mLineNum;
    public String mType;

    protected Symbol(ClassDeclaration declaration, int lineNum, String name) {
        mDeclaration = declaration;
        mLineNum = lineNum;
        mName = name;
    }

    public boolean hasVoidType() {
        return mType.equals(Type.VOID);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol s = (Symbol) obj;
            return mName.equals(s.mName) && mType.equals(s.mType);
        }
        return false;
    }

    public String toString() {
        return mName + ":" + mType;
    }
}
