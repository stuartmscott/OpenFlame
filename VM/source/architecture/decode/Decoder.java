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
package architecture.decode;

import java.util.BitSet;

import architecture.IContext;
import architecture.isa.Instruction;
import architecture.isa.special.Interrupt;

public class Decoder implements IDecoder {

    public final DecodeNode mDecodeTree;
    public volatile IContext mContext;
    public volatile long mInstruction;
    public volatile int mType;
    public volatile int mCCode;
    public volatile long mMask;
    public volatile long mConstant48Bit;
    public volatile long mConstant32Bit;
    public volatile int mRegister0;
    public volatile int mRegister1;
    public volatile int mRegister2;
    public volatile boolean mBackward;
    public volatile boolean mFloatingPoint;
    public volatile BitSet mBits;

    public Decoder(DecodeNode decodeTree) {
        this.mDecodeTree = decodeTree;
    }

    public Instruction decode(IContext context) {
        mContext = context;
        mInstruction = context.getInstruction();
        mBits = BitSet.valueOf(new long[]{mInstruction});
        mCCode = (int) ((mInstruction >> 61) & 0x3L);
        mBackward = ((int) ((mInstruction >> 60) & 0x1L)) == 1;
        mFloatingPoint = ((int) ((mInstruction >> 58) & 0x1L)) == 1;
        mType = (int) ((mInstruction >> 54) & 0xfL);
        mMask = mInstruction & 0xfffffffffffffffL;
        mConstant48Bit = (mInstruction >> 12) & 0xffffffffffffL;
        mConstant32Bit = (mInstruction >> 18) & 0xffffffffL;
        mRegister0 = (int) ((mInstruction >> 12) & 0x3fL);
        mRegister1 = (int) ((mInstruction >> 6) & 0x3fL);
        mRegister2 = (int) (mInstruction & 0x3fL);
        Instruction inst = mDecodeTree.getInst(this);
        if (inst == null) {
            inst = new Interrupt(context, Interrupt.UNSUPPORTED_OPERATION);
        }
        return inst;
    }
}
