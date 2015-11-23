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
package architecture.isa.special;

import architecture.IContext;

public class Command extends Special {

    public static final int GET_CARD_ID = 0;
    public static final int GET_CONTEXT_ID = 1;
    public static final int GET_DEVICE_ID = 2;
    public static final int GET_ERROR_ID = 3;
    public static final int HAS_LOCK = 4;
    public static final int IS_IN_INTERRUPT = 5;
    public static final int GET_IVT_ADDR = 6;
    public static final int SET_IVT_ADDR = 7;
    public static final int GET_TIME = 8;
    public static final int SET_TIME = 9;
    public static final int GET_PID = 10;
    public static final int SET_PID = 11;
    public static final int GET_CODE_BASE = 12;
    public static final int SET_CODE_BASE = 13;
    public static final int GET_STACK_START = 14;
    public static final int SET_STACK_START = 15;
    public static final int GET_STACK_LIMIT = 16;
    public static final int SET_STACK_LIMIT = 17;

    public static final int DCACHE_CLEAR = 18;
    public static final int DCACHE_FLUSH = 19;
    public static final int ICACHE_CLEAR = 20;

    public final long mType;
    public final int mRegisterIndex;

    public Command(IContext context, long type, int registerIndex) {
        super(context, "Command");
        mType = type;
        mRegisterIndex = registerIndex;
    }

    public void load() {
        mPipeline.setLoadRegister0(mContext.getRegisterBank().read(mRegisterIndex));
    }

    public void execute() {
        switch ((int) mType) {
            case GET_CARD_ID:
                mPipeline.setExecuteRegister0(mCard.getId());
                break;
            case GET_CONTEXT_ID:
                mPipeline.setExecuteRegister0(mContext.getId());
                break;
            case GET_DEVICE_ID:
                mPipeline.setExecuteRegister0(mContext.getDeviceId());
                break;
            case GET_ERROR_ID:
                mPipeline.setExecuteRegister0(mContext.getErrorId());
                break;
            case GET_IVT_ADDR:
                mPipeline.setExecuteRegister0(mCard.getInterruptTableAddress());
                break;
            case SET_IVT_ADDR:
                mCard.setInterruptTableAddress(mPipeline.getLoadRegister0());
                break;
            case GET_TIME:
                mPipeline.setExecuteRegister0(System.currentTimeMillis());
                break;
            case SET_TIME:
                System.err.println("Pointless setting time in simulator.");
                break;
            case GET_PID:
                mPipeline.setExecuteRegister0(mContext.getProcessId());
                break;
            case SET_PID:
                mContext.setProcessId(mPipeline.getLoadRegister0());
                break;
            case GET_CODE_BASE:
                mPipeline.setExecuteRegister0(mContext.getCodeBase());
                break;
            case SET_CODE_BASE:
                mContext.setCodeBase(mPipeline.getLoadRegister0());
                break;
            case GET_STACK_START:
                mPipeline.setExecuteRegister0(mContext.getStackStart());
                break;
            case SET_STACK_START:
                mContext.setStackStart(mPipeline.getLoadRegister0());
                break;
            case GET_STACK_LIMIT:
                mPipeline.setExecuteRegister0(mContext.getStackLimit());
                break;
            case SET_STACK_LIMIT:
                mContext.setStackLimit(mPipeline.getLoadRegister0());
                break;
            case DCACHE_CLEAR:
                mCard.getDataStore().clear(mPipeline.getLoadRegister0());
                mCard.getLowerStore().clear(mPipeline.getLoadRegister0());
                break;
            case DCACHE_FLUSH:
                boolean retry = mCard.getDataStore().flush(mPipeline.getLoadRegister0());
                mContext.setRetry(retry | mCard.getLowerStore().flush(mPipeline.getLoadRegister0()));
                break;
            case ICACHE_CLEAR:
                mCard.getInstructionStore().clear(mPipeline.getLoadRegister0());
                mCard.getLowerStore().clear(mPipeline.getLoadRegister0());
                break;
            default:
                mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
                break;
        }
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
    }

    public void store() {
        if (mType == GET_CARD_ID
                || mType == GET_CONTEXT_ID
                || mType == GET_DEVICE_ID
                || mType == GET_ERROR_ID
                || mType == GET_IVT_ADDR
                || mType == GET_TIME
                || mType == GET_PID
                || mType == GET_CODE_BASE
                || mType == GET_STACK_START
                || mType == GET_STACK_LIMIT) {
            mContext.getRegisterBank().write(mRegisterIndex, mPipeline.getFormatRegister0());
        }
    }

}
