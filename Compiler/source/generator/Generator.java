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
package generator;

import internalrep.assembly.AssemblyStatement;
import internalrep.assembly.controlflow.Call;
import internalrep.assembly.controlflow.Label;
import internalrep.assembly.datamovement.Copy;
import internalrep.assembly.datamovement.LoadC;
import internalrep.assembly.datamovement.Pop;
import internalrep.assembly.datamovement.Push;
import internalrep.constant.Function;

import java.util.ArrayList;
import java.util.Arrays;

import main.Compiler;

public class Generator {

    public static final int ANY_REGISTER = -1;
    public static final int GENERAL_PURPOSE_REGISTER_COUNT = 64;//MAXIMUM OF 64
    public final Register register_zero = new Register("register_zero", true);
    public final Register register_one = new Register("register_one", true);
    private final Register register_two = new Register("register_two", true);// reserved for pc
    private final Register register_three = new Register("register_three", true);// reserved for sp
    private final Register register_four = new Register("register_four", true);// used by kernel
    private final Register register_five = new Register("register_five", true);// used by kernel
    private final Register register_six = new Register("register_six", true);// used by kernel
    private final Register register_seven = new Register("register_seven", true);// used by kernel

    public ArrayList<Register> mVariableRegisters = new ArrayList<Register>();
    public ArrayList<Register> mExpressionRegisters = new ArrayList<Register>();
    private int mVariableRegisterRequestIndex = 0;
    private int mExpressionRegisterRequestIndex = 0;
    private ArrayList<RegisterMask> mMasks = new ArrayList<RegisterMask>();
    private final Function mFunction;
    private boolean[] mFreeRegs;

    public Generator(Function function) {
        mFunction = function;
        register_zero.mId = 0;
        register_one.mId = 1;
        register_two.mId = 2;
        register_three.mId = 3;
        register_four.mId = 4;
        register_five.mId = 5;
        register_six.mId = 6;
        register_seven.mId = 7;
        mFreeRegs = new boolean[GENERAL_PURPOSE_REGISTER_COUNT];
        for (int i = 8; i < GENERAL_PURPOSE_REGISTER_COUNT; i++) {
            // registers 0 to 7 are reserved
            mFreeRegs[i] = true;
        }
    }

    public Register getVariableRegister(String name) {
        Register register = new Register(name, false);
        register.mAllocationIndex = mVariableRegisterRequestIndex++;
        mVariableRegisters.add(register);
        return register;
    }

    public Register getExpressionRegister(String name) {
        Register register = new Register(name, true);
        register.mAllocationIndex = mExpressionRegisterRequestIndex++;
        mExpressionRegisters.add(register);
        return register;
    }

    public void freeVariableRegister(Register register, boolean clear) {
        if (clear) {
            emitAssembly(new Copy("clear register", register_zero, register_zero, register));// reset the register
        }
        if (register.mFreeIndex != -1 || register.mIsExpression) {
            throw new RuntimeException();
        }
        register.mFreeIndex = mVariableRegisterRequestIndex;
    }

    public void freeExpressionRegister(Register register) {
        if (register.mFreeIndex != -1 || !register.mIsExpression) {
            throw new RuntimeException();
        }
        register.mFreeIndex = mExpressionRegisterRequestIndex;
    }

    public void retainValueInRegister(Register parameter) {
        emitCoreCall("#language.Core.retain:language.Void()", parameter);
    }

    public void releaseValueInRegister(Register parameter) {
        emitCoreCall("#language.Core.release:language.Void()", parameter);
    }

    private void emitCoreCall(String function, Register parameter) {
        // save used registers
        RegisterMask registersInUseMask = new RegisterMask(this, getRegistersInUse());
        emitAssembly(new Push("save used registers", registersInUseMask));
        // push return address
        Register returnAddressRegister = getExpressionRegister("Generator.emitCoreCall() returnAddressRegister");
        Label returnLabel = new Label("return address");
        emitAssembly(new LoadC("load return address", returnLabel, returnAddressRegister));
        emitAssembly(new Push("push return address", new RegisterMask(this, returnAddressRegister)));
        freeExpressionRegister(returnAddressRegister);
        // load parameter
        emitAssembly(new Push("push parameter", new RegisterMask(this, parameter)));
        // call
        Register callRegister = getExpressionRegister("Generator.emitCoreCall() callRegister");
        emitAssembly(new LoadC("load address of function", function, callRegister));
        emitAssembly(new Call("call function", callRegister));
        freeExpressionRegister(callRegister);
        emitAssembly(returnLabel);
        // restore all registers used so far
        emitAssembly(new Pop("restore used registers", registersInUseMask));
    }

    public void allocateVariableRegisters() {
        buildGraph(mVariableRegisters);
        reduce(mVariableRegisters);
    }

    public void allocateExpressionRegisters() {
        buildGraph(mExpressionRegisters);
        reduce(mExpressionRegisters);
    }

    private void buildGraph(ArrayList<Register> registers) {
        for (Register r : registers) {
            if (r.mFreeIndex == -1) {
                Compiler.generatorError(mFunction.mDeclaration.mName + "." + mFunction.mName,
                        "register was not freed " + r);
            }
            for (int i = r.mAllocationIndex + 1; i < registers.size() && i < r.mFreeIndex; i++) {
                r.controls(registers.get(i));
            }
            r.mAvailable = Arrays.copyOf(mFreeRegs, GENERAL_PURPOSE_REGISTER_COUNT);
        }
    }

    private void reduce(ArrayList<Register> registers) {
        ArrayList<Register> registersToReduce = new ArrayList<Register>(registers);
        ArrayList<Register> newRegistersToReduce;
        while (registersToReduce.size() > 0) {
            newRegistersToReduce = new ArrayList<Register>();
            for (Register register : registersToReduce) {
                register.reduce();// this will reduce the graph
                if (register.mId == ANY_REGISTER) {
                    newRegistersToReduce.add(register);
                } else {
                    mFreeRegs[register.mId] = false;
                }
            }
            registersToReduce = newRegistersToReduce;
        }
    }

    public ArrayList<Register> getRegistersInUse() {
        ArrayList<Register> inUse = new ArrayList<Register>();
        for (Register r : mVariableRegisters) {
            if (r.mFreeIndex == -1) {
                inUse.add(r);
            }
        }
        for (Register r : mExpressionRegisters) {
            if (r.mFreeIndex == -1) {
                inUse.add(r);
            }
        }
        return inUse;
    }

    public void addMask(RegisterMask mask) {
        mMasks.add(mask);
    }

    public void emitAssembly(AssemblyStatement statement) {
        mFunction.mInstructions.add(statement);
    }

    public static long getMask(boolean[] wasUsed) {
        // TODO switch to using bitset
        long mask = 0;
        for (int i = 0; i < wasUsed.length; i++) {
            mask = mask | ((wasUsed[i] ? 1L : 0L) << i);
        }
        return mask;
    }

}
