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
import generator.Register;
import internalrep.assembly.datamovement.Copy;
import internalrep.declaration.ClassDeclaration;
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;

public class AssemblyAssign extends Statement {

    private final AssemblyList mBlock;
    private final int mRegisterId;
    private final LocalVariable mVariable;

    public AssemblyAssign(ClassDeclaration declaration, int l, AssemblyList block, int registerId, LocalVariable variable) {
        super(declaration, l);
        mVariable = variable;
        mRegisterId = registerId;
        mBlock = block;
    }

    @Override
    public void resolveType(SymbolTable st) {
        st.addSymbol(mVariable, mDeclaration.mName, mLineNum);
        mBlock.resolveType(st);
    }

    @Override
    public void emit(Generator g) {
        mBlock.emit(g);
        Register sourceRegister = new Register("AssemblyAssign.emit() sourceRegister", true);
        sourceRegister.mId = mRegisterId;
        g.emitAssembly(new Copy("save assembly value", sourceRegister, g.register_zero, mVariable.bindToNewRegister(g)));
    }

    @Override
    public String toString() {
        return mBlock + " r" + mRegisterId + " " + mVariable;
    }

    @Override
    public boolean returns() {
        return false;
    }

}
