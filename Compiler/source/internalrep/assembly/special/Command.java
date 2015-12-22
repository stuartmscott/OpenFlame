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
package internalrep.assembly.special;

import generator.Register;
import linker.Linker;

public class Command extends Special {

    public static final int GET_CARD_ID = 0;
    public static final int GET_CONTEXT_ID = 1;
    public static final int GET_DEVICE_ID = 2;
    public static final int GET_ERROR_ID = 3;
    public static final int HAS_LOCK = 4;
    public static final int IS_IN_INTERRUPT = 5;
    public static final int GET_INTERRUPT_TABLE_ADDRESS = 6;
    public static final int SET_INTERRUPT_TABLE_ADDRESS = 7;
    public static final int GET_TIME = 8;
    public static final int SET_TIME = 9;
    public static final int GET_PROCESS_ID = 10;
    public static final int SET_PROCESS_ID = 11;
    public static final int GET_CODE_BASE = 12;
    public static final int SET_CODE_BASE = 13;
    public static final int GET_STACK_START = 14;
    public static final int SET_STACK_START = 15;
    public static final int GET_STACK_LIMIT = 16;
    public static final int SET_STACK_LIMIT = 17;

    public static final int DATA_CACHE_CLEAR = 18;
    public static final int DATA_CACHE_FLUSH = 19;
    public static final int INSTRUCTION_CACHE_CLEAR = 20;

    private long mCommandType;
    private String mCommandConstant;
    private int mParameterRegisterIndex;
    private Register mParameter;

    public Command(String comment, long commandType, Register parameter) {
        super(comment, COMMAND);
        mCommandType = commandType;
        mParameter = parameter;
        mIsRegister = true;
    }

    public Command(String comment, long commandType, int parameterRegisterIndex) {
        super(comment, COMMAND);
        mCommandType = commandType;
        mParameterRegisterIndex = parameterRegisterIndex;
    }

    public Command(String comment, String commandConstant, int parameterRegisterIndex) {
        super(comment, COMMAND);
        mCommandConstant = commandConstant;
        mParameterRegisterIndex = parameterRegisterIndex;
    }

    private void toIndicies() {
        if (mIsRegister) {
            mParameterRegisterIndex = mParameter.getHardwareRegister();
        }
    }

    public void link(Linker l) {
        if (mCommandConstant != null) {
            mCommandType = l.getConstant(mCommandConstant);
        }
    }

    public long emit() {
        toIndicies();
        return super.emit() | (mCommandType << 18L) | (mParameterRegisterIndex);
    }

    public String toString() {
        toIndicies();
        if (mCommandConstant != null) {
            return "cmd " + mCommandConstant + " r" + mParameterRegisterIndex + super.toString();
        }
        return "cmd " + mCommandType + " r" + mParameterRegisterIndex + super.toString();
    }

}
