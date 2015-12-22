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
import internalrep.symbol.SymbolTable;
import internalrep.declaration.ClassDeclaration;

import java.util.ArrayList;

public class StatementList extends Statement {

    public ArrayList<Statement> mStatements = new ArrayList<Statement>();
    private boolean mReturnFound = false;

    public StatementList(ClassDeclaration declaration, int lineNum) {
        super(declaration, lineNum);
    }

    public void add(Statement statement) {
        if (statement instanceof ReturnStatement) {
            mReturnFound = true;
        }
        mStatements.add(statement);
    }

    @Override
    public void resolveType(SymbolTable st) {
        st = new SymbolTable(st);// create a new scope
        for (Statement s : mStatements) {
            s.resolveType(st);
        }
        if (!returns()) {
            SymbolFree.addFrees(mDeclaration, mLineNum, this, st);
        }
    }

    @Override
    public void emit(Generator g) {
        for (Statement s : mStatements) {
            s.emit(g);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        for (Statement s : mStatements) {
            sb.append(s + "\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean returns() {
        for (Statement s : mStatements) {
            if (s.returns()) {
                return true;
            }
        }
        return false;
    }
}
