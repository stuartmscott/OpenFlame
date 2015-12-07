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
package internalrep.asm.special;

import linker.Linkable;
import linker.Linker;

public class Command extends Special implements Linkable {

    public static final int GET_CARD_ID = 0;
    public static final int GET_CONTEXT_ID = 1;
    public static final int GET_DEVICE_ID = 2;
    public static final int GET_ERROR_ID = 3;
    public static final int GET_IVT_ADDR = 4;
    public static final int SET_IVT_ADDR = 5;
    public static final int GET_TIME = 6;
    public static final int SET_TIME = 7;
    public static final int GET_PID = 8;
    public static final int SET_PID = 9;
    public static final int GET_CODE_BASE = 10;
    public static final int SET_CODE_BASE = 11;
    public static final int GET_STACK_START = 12;
    public static final int SET_STACK_START = 13;
    public static final int GET_STACK_LIMIT = 14;
    public static final int SET_STACK_LIMIT = 15;

    public static final int DCACHE_CLEAR = 16;
    public static final int DCACHE_FLUSH = 17;
    public static final int ICACHE_CLEAR = 18;

    private long cmdType;
    private String cmdConst;
    private int paramRegIndex;

    public Command(long cmdType, int paramRegIndex, String comment) {
        super(CMD, comment);
        this.cmdType = cmdType;
        this.paramRegIndex = paramRegIndex;
    }

    public Command(String cmdConst, int paramRegIndex, String comment) {
        super(CMD, comment);
        this.cmdConst = cmdConst;
        this.paramRegIndex = paramRegIndex;
    }

    public void link(Linker linker) {
        if (cmdConst != null) {
            cmdType = linker.getConstant(cmdConst);
        }
    }

    public long emit() {
        return super.emit() | (cmdType << 18L) | (paramRegIndex);
    }

    public String toString() {
        if (cmdConst != null) {
            return "cmd " + cmdConst + " r" + paramRegIndex + super.toString();
        }
        return "cmd " + cmdType + " r" + paramRegIndex + super.toString();
    }

}
