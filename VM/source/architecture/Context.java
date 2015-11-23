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
package architecture;

import architecture.cache.ICache;
import architecture.isa.Instruction;
import architecture.isa.special.Interrupt;

public class Context implements IContext {

    public static final int NUM_GP_REGS = 64;// MAXIMUM OF 64
    private static final long WAIT = Long.parseLong(
            "0000010010000000000000000000000000000000000000000000000000000000", 2);
    private static final long INTR = Long.parseLong(
            "0000010110000000000000000000000000000000000000000000000000000000", 2);

    public volatile boolean mRequiresLock = false;
    public volatile boolean mHasLock = false;
    public volatile boolean mValid = false;
    public volatile boolean mAsleep = true;// Start off asleep.
    public volatile boolean mIsRetry = false;

    public final int mId;
    public final ICard mCard;
    public final IRegisterBank mRegisterBank;
    public volatile long mNextStage = 1;
    public volatile long mProcessId = -1;
    public volatile long mCodeBase = 0;
    public volatile long mStackStart = 0;
    public volatile long mStackLimit = 0;
    public volatile long mProgramCounter = 0;
    public volatile long mInstruction = 0;
    public volatile Instruction mCurrentInstruction;

    public volatile boolean mIsHandlingInterrupt = false;
    public volatile long mNextInterrupt = -1;// The interrupt we are about to handle.
    public volatile long mDeviceId = -1;// The device that signaled us.
    public volatile long mErrorId = -1;
    public volatile long mNextError = -1;// The error waiting to be handled.
    public volatile long mSleepCycles = 0;

    public Context(int id, ICard card) {
        mId = id;
        mCard = card;
        mRegisterBank = new RegisterBank(this, NUM_GP_REGS);
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public long getInstruction() {
        return mInstruction;
    }

    @Override
    public ICard getCard() {
        return mCard;
    }

    @Override
    public IRegisterBank getRegisterBank() {
        return mRegisterBank;
    }

    @Override
    public long getDeviceId() {
        return mDeviceId;
    }

    @Override
    public long getErrorId() {
        return mErrorId;
    }

    @Override
    public long getProgramCounter() {
        return mProgramCounter;
    }

    @Override
    public boolean isRetry() {
        return mIsRetry;
    }

    @Override
    public void setRetry(boolean retry) {
        mIsRetry = retry;
    }

    @Override
    public boolean isHandlingInterrupt() {
        return mIsHandlingInterrupt;
    }

    @Override
    public void setIsHandlingInterrupt(boolean isHandling) {
        mIsHandlingInterrupt = isHandling;
    }

    @Override
    public boolean requiresLock() {
        return mRequiresLock;
    }

    @Override
    public void setRequiresLock(boolean required) {
        mRequiresLock = required;
    }

    @Override
    public boolean hasLock() {
        return mHasLock;
    }

    @Override
    public void setHasLock(boolean hasLock) {
        mHasLock = hasLock;
    }

    @Override
    public long getProcessId() {
        return mProcessId;
    }

    @Override
    public void setProcessId(long processId) {
        mProcessId = processId;
    }

    @Override
    public long getCodeBase() {
        return mCodeBase;
    }

    @Override
    public void setCodeBase(long codeBase) {
        mCodeBase = codeBase;
    }

    @Override
    public long getStackStart() {
        return mStackStart;
    }

    @Override
    public void setStackStart(long address) {
        mStackStart = address;
    }

    @Override
    public long getStackLimit() {
        return mStackLimit;
    }

    @Override
    public void setStackLimit(long address) {
        mStackLimit = address;
    }

    @Override
    public void fetchInst() {
        mValid = true;
        if (mNextError != -1) {
            mNextInterrupt = Interrupt.ERROR_OCCURED;
            mErrorId = mNextError;
            mNextError = -1;
            mIsRetry = false;
        } else if (!mIsRetry) {
            mCurrentInstruction = null;
            if (!mIsHandlingInterrupt && !mHasLock && mCard.getSignal() != -1) {
                mNextInterrupt = Interrupt.SIGNAL_RECEIVED;
                mDeviceId = mCard.getSignal();
                mCard.clearSignal();
                mAsleep = false;
                System.out.println("Asleep for " + mSleepCycles + " cycles.");
                mSleepCycles = 0;
            } else if (!mAsleep) {
                mProgramCounter = mRegisterBank.read(2);
                ICache store = mCard.getInstructionStore();
                if (!store.isBusy()) {
                    store.read(mProgramCounter);
                }
            } else {
                mSleepCycles++;
            }
        }
        stageTrace("0 pc:" + mProgramCounter);
        mNextStage++;
    }

    @Override
    public void loadInstruction() {
        if (mValid && !mIsRetry && !mAsleep) {
            if (!mIsHandlingInterrupt && mNextInterrupt != -1) {
                mInstruction = INTR | (mNextInterrupt << 18L);
                mIsHandlingInterrupt = true;
                mNextInterrupt = -1;
            } else {
                ICache store = mCard.getInstructionStore();
                if (!store.isBusy() && store.success()) {
                    mInstruction = store.getBus().mValues[0];
                } else {
                    // Could get rid of wait, just set valid to false and log a
                    // wasted cycle.
                    mInstruction = WAIT;
                }
            }
        }
        stageTrace("1 ir:" + Long.toBinaryString(mInstruction));
        mNextStage++;
    }

    @Override
    public void decode() {
        if (mValid && !mAsleep) {
            if (mIsRetry) {
                mCurrentInstruction.mIsRetry = true;
                mIsRetry = false;
            } else {
                mCurrentInstruction = mCard.getDecoder().decode(this);
            }
        }
        stageTrace("2 inst:" + mCurrentInstruction);
        mNextStage++;
    }

    @Override
    public void load() {
        if (mValid && !mAsleep) {
            mCurrentInstruction.load();
        }
        stageTrace("3");
        mNextStage++;
    }

    @Override
    public void execute() {
        if (mValid && !mAsleep) {
            mCurrentInstruction.execute();
        }
        stageTrace("4");
        mNextStage++;
    }

    @Override
    public void format() {
        if (mValid && !mAsleep) {
            mCurrentInstruction.format();
        }
        stageTrace("5");
        mNextStage++;
    }

    @Override
    public void store() {
        if (mValid && !mAsleep) {
            mCurrentInstruction.store();
        }
        stageTrace("6");
        mNextStage++;
    }

    @Override
    public void retire() {
        if (mValid && !mAsleep) {
            mCurrentInstruction.retire();
        }
        stageTrace("7");
        mNextStage = 0;
    }

    @Override
    public void error(long errorId) {
        if (mIsHandlingInterrupt) {
            throw new RuntimeException("Double Interrupt");
        }
        Utilities.trace(Utilities.CONTEXT_INTERRUPT, "Card " + mCard.getId() + " Context " + mId + " got error " + errorId);
        mNextError = errorId;
    }

    @Override
    public void sleep() {
        if (mIsHandlingInterrupt) {
            System.err.println("Sleep issued during interrupt handler.");
        }
        mAsleep = true;
    }

    private void stageTrace(String stage) {
        Utilities.trace(Utilities.CONTEXT_STAGES, mCard.getId() + " : " + mId + " stage:" + stage);
    }
}
