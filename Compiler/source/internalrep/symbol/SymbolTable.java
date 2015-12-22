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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import main.Compiler;

public class SymbolTable {

    private HashMap<String, LocalVariable> mTop = new HashMap<String, LocalVariable>();
    private SymbolTable mRest;

    public SymbolTable(SymbolTable rest) {
        mRest = rest;
    }

    public Collection<LocalVariable> allTopSymbols(){
        return new ArrayList<LocalVariable>(mTop.values());
    }

    public LocalVariable getSymbol(String name) {
        LocalVariable sym = mTop.get(name);
        if ((mRest != null) && (sym == null)) {
            sym = mRest.getSymbol(name);
        }
        return sym;
    }

    public void addSymbol(LocalVariable s, String decName, int lineNum) {
        if (mTop.containsKey(s.mName)) {
            Compiler.syntaxError(decName, lineNum, s + " was already declared in this scope");
        }
        mTop.put(s.mName, s);
    }

    public void deleteSymbol(LocalVariable s) {
        mTop.remove(s.mName);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("----------\n");
        for (LocalVariable s : mTop.values()) {
            sb.append(s + "\n");
        }
        if (mRest != null) {
            sb.append(mRest.toString());
        }
        return sb.toString();
    }

}
