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
package internalrep.expression;

import generator.Generator;
import generator.Register;
import generator.RegisterMask;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.assembly.datamovement.Store;
import internalrep.assembly.special.Interrupt;
import internalrep.declaration.ClassDeclaration;
import internalrep.declaration.Declaration;
import internalrep.declaration.ProtocolDeclaration;
import internalrep.symbol.SymbolTable;

import java.util.Set;

import main.Compiler;

public class ProtocolCast extends Expression {

    private final Expression mExpression;
    private final String mType;
    private ProtocolDeclaration mProtocolType;
    private Declaration mExpressionType;

    public ProtocolCast(ClassDeclaration declaration, int lineNum, Expression expression, String type) {
        super(declaration, lineNum);
        // TODO if type is already a protocol, need to copy the inst and lookup the methods
        mExpression = expression;
        mType = type;
    }

    public String resolveType(SymbolTable st) {
        Declaration d = mDeclaration.mCompiler.getDeclaration(mType);
        if (!(d instanceof ProtocolDeclaration)) {
            Compiler.semanticError(mDeclaration.mName, mLineNum, mType + " is not a Protocol");
        } else {
            mProtocolType = (ProtocolDeclaration) d;
        }
        mExpressionType = mDeclaration.mCompiler.getDeclaration(mExpression.getType(st));
        mProtocolType.addImplementer(mExpressionType, mLineNum);
        return mType;
    }

    public Register retainIntoRegister(Generator g) {
        //TODO this needs re-doing
        Set<String> functions = mProtocolType.getFunctions();
        int size = ClassDeclaration.HEADER_SIZE + 1 + functions.size();
        RegisterMask registersInUseMask = new RegisterMask(g, g.getRegistersInUse());
        g.emitAssembly(new Push("save used registers", registersInUseMask));

        Register destinationRegister = g.getVariableRegister("ProtocolCast.retainIntoRegister() destinationRegister");

        g.emitAssembly(new LoadC("load size", size, destinationRegister));
        g.emitAssembly(new Push("push param", new RegisterMask(g, destinationRegister)));

        // TODO should use lang.Core.create
        g.emitAssembly(new Interrupt("allocate", Interrupt.KERNEL_ALLOCATE));

        g.emitAssembly(new Pop("pop result", new RegisterMask(g, destinationRegister)));

        g.emitAssembly(new Pop("restore used registers", registersInUseMask));

        // TODO if destinationRegister contains 0, handle

        // set the type pointer
        Register typeRegister = g.getExpressionRegister("ProtocolCast.retainIntoRegister() typeRegister");
        // TODO address is offset in code, need to add the codebase
        g.emitAssembly(new LoadC("load type address", "#" + mType, typeRegister));
        g.emitAssembly(new Store("set type", destinationRegister, ClassDeclaration.TYPE_OFFSET, typeRegister));
        g.freeExpressionRegister(typeRegister);

        // set num refs
        g.emitAssembly(new Store("set num vars", destinationRegister, ClassDeclaration.NUM_REFERENCES_OFFSET, g.register_one));

        Register instanceRegister = mExpression.retainIntoRegister(g);
        g.emitAssembly(new Store("store instance", destinationRegister, ClassDeclaration.HEADER_SIZE, instanceRegister));
        g.freeVariableRegister(instanceRegister, true);

        for (String name : functions) {
            Register offsetRegister = g.getExpressionRegister("ProtocolCast.retainIntoRegister() offsetRegister");
            System.out.println(name);
            //TODO these offsets must be from the start of the class not from the start of the code
            g.emitAssembly(new LoadC("load function address", "RAWR", offsetRegister));
            g.emitAssembly(new Store("store at offset", destinationRegister, mProtocolType.getOffset(name), offsetRegister));
            g.freeExpressionRegister(offsetRegister);
        }
        return destinationRegister;
    }

    public String toString() {
        return "(" + mType + " " + mExpression + ")";
    }

}
