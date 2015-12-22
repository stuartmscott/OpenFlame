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
import internalrep.assembly.AssemblyBlock;
import internalrep.assembly.AssemblyListBlock;
import internalrep.assembly.AssemblyStatement;
import internalrep.declaration.ClassDeclaration;
import internalrep.symbol.SymbolTable;

import java.util.ArrayList;

public class AssemblyList extends Statement {

    private final ArrayList<AssemblyBlock> mBlocks;

    public AssemblyList(ClassDeclaration declaration, int lineNum, ArrayList<AssemblyBlock> blocks) {
        super(declaration, lineNum);
        mBlocks = blocks;
    }

    public AssemblyList(ClassDeclaration declaration, int lineNum, AssemblyStatement statement) {
        super(declaration, lineNum);
        mBlocks = new ArrayList<AssemblyBlock>();
        mBlocks.add(new AssemblyListBlock(statement));
    }

    @Override
    public void emit(Generator g) {
        ArrayList<AssemblyStatement> assembly = new ArrayList<AssemblyStatement>();
        for (AssemblyBlock b : mBlocks) {
            b.emit(assembly);
        }
        for (AssemblyStatement s : assembly) {
            g.emitAssembly(s);
        }
    }

    @Override
    public void resolveType(SymbolTable st) {
        // Do nothing
    }

    @Override
    public boolean returns() {
        // TODO this is dangerous
        return false;
    }

    @Override
    public String toString() {
        return "<AssemblyList>";
    }
}
