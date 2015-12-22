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
package internalrep.assembly;

import internalrep.constant.Literal;
import internalrep.type.Type;
import java.util.ArrayList;
import java.util.HashMap;
import main.Compiler;

public class AssemblyDeclarationBlock extends AssemblyBlock {

    private Compiler mCompiler;
    private final String mFullname;

    public AssemblyDeclarationBlock(String fullname) {
        mFullname = fullname;
    }

    public void setCompiler(Compiler compiler) {
        mCompiler = compiler;
    }

    @Override
    public void emit(ArrayList<AssemblyStatement> assembly) {
        mCompiler.getDeclaration(mFullname).emit(assembly);
        if (mFullname.equals(Type.NUMBER) || mFullname.equals(Type.STRING)) {
            // Emit the literals
            HashMap<String, Literal> ls = mCompiler.getLiterals();
            for (String name : ls.keySet()) {
                if (name.startsWith(mFullname)) {
                    ls.get(name).emit(assembly);
                }
            }
        }
    }

}
