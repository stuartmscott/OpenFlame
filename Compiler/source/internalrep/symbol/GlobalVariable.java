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

import internalrep.declaration.ClassDeclaration;
import main.Compiler;

public class GlobalVariable extends Symbol {

    private int mObjectOffset = -1;
    public boolean mInitialised = false;

    public GlobalVariable(ClassDeclaration declaration, int lineNum, String name, String type) {
        super(declaration, lineNum, name);
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public int getOffset() {
        if (mObjectOffset == -1) {
            Compiler.generatorError(mDeclaration.mName, "Offset was not generated for " + mName);
        }
        return mObjectOffset;
    }

    public void setOffset(int objectOffset) {
        mObjectOffset = objectOffset;
    }

}
