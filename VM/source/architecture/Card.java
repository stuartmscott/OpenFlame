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
import architecture.cache.SetAssociativeCache;
import architecture.decode.DecodeNode;
import architecture.decode.Decoder;
import architecture.decode.IDecoder;
import architecture.isa.Instruction;
import architecture.isa.arithmetic.Add;
import architecture.isa.arithmetic.AluInst;
import architecture.isa.arithmetic.Convert;
import architecture.isa.arithmetic.Divide;
import architecture.isa.arithmetic.Modulo;
import architecture.isa.arithmetic.Multiply;
import architecture.isa.arithmetic.Subtract;
import architecture.isa.controlflow.Call;
import architecture.isa.controlflow.Jump;
import architecture.isa.controlflow.Return;
import architecture.isa.datamovement.Load;
import architecture.isa.datamovement.LoadC;
import architecture.isa.datamovement.Pop;
import architecture.isa.datamovement.Push;
import architecture.isa.datamovement.Store;
import architecture.isa.special.BreakPoint;
import architecture.isa.special.Command;
import architecture.isa.special.Halt;
import architecture.isa.special.Interrupt;
import architecture.isa.special.InterruptReturn;
import architecture.isa.special.Lock;
import architecture.isa.special.Noop;
import architecture.isa.special.Signal;
import architecture.isa.special.Sleep;
import architecture.isa.special.Special;
import architecture.isa.special.Unlock;
import architecture.isa.special.Wait;
import architecture.memory.MemoryPort;

public class Card extends Clockable implements ICard {

    public static final int NUM_CONTEXTS = 8;// Do not change

    public volatile boolean mRequiresLock = false;
    public volatile boolean mHasLock = false;
    public volatile int mPrevious = 0;
    public volatile int mLockHolder = -1;

    public final int mId;
    public final IMotherboard mMotherboard;
    public final MemoryPort mPort;
    public final SetAssociativeCache mInstructionStore;
    public final SetAssociativeCache mDataStore;
    public final SetAssociativeCache mLowerStore;
    // public final StackCache stackStore;
    public final IPipeline mPipeline;
    public final IDecoder mDecoder;
    public final IContext[] mContexts;
    public volatile int mContextIndex = 0;
    public volatile long mInterruptTableAddress = 0;

    public volatile int mSignal = -1;

    public Card(int id, IMotherboard architecture) {
        super(1);
        this.mId = id;
        mMotherboard = architecture;
        mPort = new MemoryPort(architecture, 4);
        mPipeline = new Pipeline();
        mDecoder = new Decoder(makeDecodeTree());
        mContexts = new Context[NUM_CONTEXTS];
        for (int i = 0; i < NUM_CONTEXTS; i++) {
            mContexts[i] = new Context(i, this);
        }
        // 16MB l2 data cache
        mLowerStore = new SetAssociativeCache(10, 2, 4, mPort, 16 * 1024 * 1024, 16);
        // 512KB l1 instruction cache
        mInstructionStore = new SetAssociativeCache(1, 1, 2, mLowerStore, 512 * 1024, 8);
        mInstructionStore.setReadOnly();
        // 512KB l1 data cache
        mDataStore = new SetAssociativeCache(1, 1, 2, mLowerStore, 512 * 1024, 8);
        // 8KB stack cache
        // stackStore = new StackCache(1, 8 * 1024);
        mClockableTasks.add(mInstructionStore);
        mClockableTasks.add(mDataStore);
        // clockableTasks.add(stackStore);
        mClockableTasks.add(mLowerStore);
        mClockableTasks.add(mPort);
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public IMotherboard getMotherboard() {
        return mMotherboard;
    }

    @Override
    public IPipeline getPipeline() {
        return mPipeline;
    }

    @Override
    public IDecoder getDecoder() {
        return mDecoder;
    }

    @Override
    public ICache getInstructionStore() {
        return mInstructionStore;
    }

    @Override
    public ICache getDataStore() {
        return mDataStore;
    }

    @Override
    public ICache getLowerStore() {
        return mLowerStore;
    }

    public void clock() {
        if (mLockHolder == -1) {
            int index;
            for (int i = 0; mLockHolder == -1 && i < NUM_CONTEXTS; i++) {
                index = (i + mPrevious) % NUM_CONTEXTS;
                if (mContexts[index].requiresLock()) {
                    if (mHasLock) {
                        mContexts[index].setHasLock(true);
                        mLockHolder = index;
                    } else {
                        mRequiresLock = true;
                    }
                }
            }
        } else {
            if (!mContexts[mLockHolder].requiresLock()) {
                mContexts[mLockHolder].setHasLock(false);
                mRequiresLock = false;
                mPrevious = mLockHolder;
                mLockHolder = -1;
            }
        }
        // Run the pipeline in reverse so data flow in intermediate registers are not affected.
        getCurrentContext(7).retire();
        getCurrentContext(6).store();
        getCurrentContext(5).format();
        getCurrentContext(4).execute();
        getCurrentContext(3).load();
        getCurrentContext(2).decode();
        getCurrentContext(1).loadInstruction();
        getCurrentContext(0).fetchInst();
        mContextIndex = (mContextIndex + 1) % NUM_CONTEXTS;
    }

    private IContext getCurrentContext(int stageNum) {
        int index = mContextIndex - stageNum;
        if (index < 0) {
            index += NUM_CONTEXTS;
        }
        return mContexts[index];
    }

    @Override
    public int getSignal() {
        return mSignal;
    }

    @Override
    public boolean signal(int id) {
        int old = mSignal;
        mSignal = id;
        return old == -1;
    }

    @Override
    public void clearSignal() {
        mSignal = -1;
    }

    private static DecodeNode makeDecodeTree() {
        DecodeNode loadc = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new LoadC(d.mContext, d.mConstant48Bit, d.mRegister2);
            }
        };
        DecodeNode store = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Store(d.mContext, d.mRegister1, d.mConstant48Bit, d.mRegister2);
            }
        };
        DecodeNode load = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Load(d.mContext, d.mRegister1, d.mConstant48Bit, d.mRegister2);
            }
        };
        DecodeNode pop = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Pop(d.mContext, d.mMask);
            }
        };
        DecodeNode push = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Push(d.mContext, d.mMask);
            }
        };
        DecodeNode alu = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                switch (d.mType) {
                    case AluInst.ADD:
                        return new Add(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                    case AluInst.SUB:
                        return new Subtract(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                    case AluInst.MUL:
                        return new Multiply(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                    case AluInst.DIV:
                        return new Divide(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                    case AluInst.MOD:
                        return new Modulo(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                    case AluInst.CONVERT:
                        return new Convert(d.mContext, d.mFloatingPoint, d.mRegister0, d.mRegister1, d.mRegister2);
                }
                return null;
            }
        };

        DecodeNode hwcontrol = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                switch (d.mType) {
                    case Special.HALT:
                        return new Halt(d.mContext);
                    case Special.SLEEP:
                        return new Sleep(d.mContext);
                    case Special.WAIT:
                        return new Wait(d.mContext);
                    case Special.NOOP:
                        return new Noop(d.mContext);
                    case Special.COMMAND:
                        return new Command(d.mContext, d.mConstant32Bit, d.mRegister2);
                    case Special.SIGNAL:
                        return new Signal(d.mContext, d.mRegister2);
                    case Special.INTERRUPT:
                        return new Interrupt(d.mContext, d.mConstant32Bit);
                    case Special.INTERRUPT_RETURN:
                        return new InterruptReturn(d.mContext, d.mRegister2);
                    case Special.LOCK:
                        return new Lock(d.mContext);
                    case Special.UNLOCK:
                        return new Unlock(d.mContext);
                    case Special.BREAK:
                        return new BreakPoint(d.mContext, d.mConstant32Bit);
                }
                return null;
            }
        };
        DecodeNode jump = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Jump(d.mContext, d.mBackward, d.mCCode, d.mConstant48Bit, d.mRegister1, d.mRegister2);
            }
        };
        DecodeNode call = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Call(d.mContext, d.mRegister2);
            }
        };
        DecodeNode ret = new DecodeNode() {
            public Instruction getInst(Decoder d) {
                return new Return(d.mContext, d.mRegister2);
            }
        };
        DecodeNode pushPop = new DecodeNode(60, push, pop);
        DecodeNode loadStore = new DecodeNode(60, store, load);
        DecodeNode dataMove = new DecodeNode(61, pushPop, loadStore);
        DecodeNode callRet = new DecodeNode(56, call, ret);
        return new DecodeNode(63, jump, new DecodeNode(62, dataMove, new DecodeNode(60, loadc,
                        new DecodeNode(59, alu, new DecodeNode(58, hwcontrol, callRet)))));
    }

    @Override
    public long getInterruptTableAddress() {
        return mInterruptTableAddress;
    }

    @Override
    public void setInterruptTableAddress(long address) {
        mInterruptTableAddress = address;
    }
}
