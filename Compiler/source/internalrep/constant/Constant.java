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
package internalrep.constant;

import internalrep.assembly.controlflow.Label;
import internalrep.declaration.ClassDeclaration;

public class Constant {

    public final ClassDeclaration mDeclaration;
    public final int mLineNum;
    public final String mName;
    public String mType;
    public Label mAddress;

    public Constant(ClassDeclaration declaration, int lineNum, String name) {
        mDeclaration = declaration;
        mLineNum = lineNum;
        mName = name;
        mAddress = new Label("", "#" + name);
    }
}
