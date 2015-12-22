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

import internalrep.assembly.controlflow.Call;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.declaration.ClassDeclaration;
import internalrep.symbol.SymbolTable;
import generator.Generator;
import generator.RegisterMask;
import generator.Register;

public class LiteralReference extends Reference {

    protected final String mOwner;
    protected final String mName;
    protected String mType;
    protected String mLabel;

    public LiteralReference(ClassDeclaration declaration, int lineNum, String owner, String name, String type) {
        super(declaration, lineNum);
        mOwner = owner;
        mName = name;
        mType = type;
        mLabel = "#" + mName;
    }

    public String resolveType(SymbolTable st) {
        return mType;
    }

    public Register retainIntoRegister(Generator g) {
        RegisterMask registersInUseMask = new RegisterMask(g, g.getRegistersInUse());
        g.emitAssembly(new Push("save used registers", registersInUseMask));

        // push return address
        Register returnAddressRegister = g.getExpressionRegister("LiteralReference.retainIntoRegister() returnAddressRegister");
        Label returnLabel = new Label("return address");
        g.emitAssembly(new LoadC("load return address", returnLabel, returnAddressRegister));
        g.emitAssembly(new Push("push return address", new RegisterMask(g, returnAddressRegister)));
        g.freeExpressionRegister(returnAddressRegister);

        // push parameter
        Register literalRegister = g.getExpressionRegister("LiteralReference.emitIntoRegister() literalRegister");
        g.emitAssembly(new LoadC("load literal address", mLabel, literalRegister));
        g.emitAssembly(new Push("push address", new RegisterMask(g, literalRegister)));
        g.freeExpressionRegister(literalRegister);

        // calc function address
        Register destinationRegister = g.getVariableRegister("LiteralReference.emitIntoRegister() destinationRegister");
        String t = mType.equals("language.Number") ? "Number" : "String";
        String f = "#language.Core.load" + t + "Literal:language.Void()";
        g.emitAssembly(new LoadC("load function address", f, destinationRegister));

        g.emitAssembly(new Call("call function", destinationRegister));
        g.emitAssembly(returnLabel);
        g.emitAssembly(new Pop("pop result", new RegisterMask(g, destinationRegister)));
        // Restore all registers used so far
        g.emitAssembly(new Pop("restore used registers", registersInUseMask));
        return destinationRegister;
    }

    public String toString() {
        return mName;
    }

}
