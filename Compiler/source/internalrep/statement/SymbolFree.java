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
import internalrep.symbol.LocalVariable;
import internalrep.symbol.SymbolTable;
import internalrep.declaration.ClassDeclaration;

import java.util.Collection;

import main.Compiler;

public class SymbolFree extends Statement {

    private final Collection<LocalVariable> mSymbols;

    public SymbolFree(ClassDeclaration declaration, int lineNum, Collection<LocalVariable> symbols) {
        super(declaration, lineNum);
        mSymbols = symbols;
    }

    @Override
    public void resolveType(SymbolTable st) {
        // Do nothing
    }

    @Override
    public void emit(Generator g) {
        for (LocalVariable symb : mSymbols) {
            if (symb.getBoundRegister() == null) {
                Compiler.generatorError(mDeclaration.mName, "cannot free " + symb
                        + ", symbol already freed");
            }
            symb.unbindFromRegister(g, true);
        }
    }

    @Override
    public boolean returns() {
        return false;
    }

    @Override
    public String toString() {
        return "free&release " + mSymbols;
    }

    public static void addFrees(ClassDeclaration declaration, int lineNum, StatementList statements, SymbolTable st) {
        Collection<LocalVariable> list = st.allTopSymbols();
        if (list.size() > 0) {
            statements.add(new SymbolFree(declaration, lineNum, list));
        }
    }

}
