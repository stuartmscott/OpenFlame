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
package architecture.cache;

import java.util.Arrays;
import java.util.BitSet;

import architecture.Utilities;
import architecture.memory.MemorySubsystem;

public class SetAssociativeCache extends HierarchicalCache {

    public final CacheBlock[] mBlocks;
    public final int mAssociativity;
    public final int mNumLines;
    public final int mNumTagBits;
    public final int mNumIndexBits;
    public final int mNumOffsetBits;
    public boolean mReadOnly;

    public SetAssociativeCache(int numClocks, int busSize, int lineSize, MemorySubsystem lower,
            int size, int associativity) {
        super(numClocks, busSize, lineSize, lower);
        mAssociativity = associativity;
        mNumLines = ((size / associativity) / 8) / lineSize;
        mBlocks = new CacheBlock[associativity];
        for (int i = 0; i < associativity; i++) {
            mBlocks[i] = new CacheBlock(mNumLines, lineSize);
        }

        mNumOffsetBits = (int) Utilities.log2(lineSize);
        mNumIndexBits = (int) Utilities.log2(mNumLines);
        mNumTagBits = 32 - mNumIndexBits - mNumOffsetBits;
    }

    @Override
    public void setReadOnly() {
        mReadOnly = true;
    }

    @Override
    protected void handleRequest() {
        final int[] cacheAddr = addrToCacheAddr(mAddress);
        final int tag = cacheAddr[0];
        final int index = cacheAddr[1];
        final int offset = cacheAddr[2];
        mSuccess = false;
        CacheLine l = null;
        for (int b = 0; !mSuccess && b < mBlocks.length; b++) {
            l = mBlocks[b].mLines[index];
            if (l.mTag == tag) {
                mSuccess = true;
                mBlocks[b].setRecentlyUsed(index);
            }
        }

        if (mRead) {
            for (int i = 0; i < mBusSize; i++) {
                if (!l.mData.mValid[i + offset]) {
                    mSuccess = false;
                }
            }
            if (mSuccess) {
                for (int i = 0; i < mBusSize; i++) {
                    getBus().writeValue(i, l.mData.mValues[offset + i]);
                }
            } else {
                lowerRead(cacheAddrToAddr(tag, index, 0));// issue read to lower level
            }
        } else if (mWrite) {
            if (mReadOnly) {
                throw new RuntimeException("Attempted to write to an read-only cache");
            }
            if (!mSuccess) {
                l = pickVictim(tag, index);
            }
            mSuccess = true;
            getBus().writeTo(l.mData, offset);
            l.mRecentlyUsed = true;
        }
    }

    public int[] addrToCacheAddr(long address) {
        BitSet b = BitSet.valueOf(new long[] { address });
        int i = 0;
        long[] l = b.get(i, mNumOffsetBits).toLongArray();
        final int offset = (l.length > 0) ? (int) l[0] : 0;
        i += mNumOffsetBits;
        l = b.get(i, i + mNumIndexBits).toLongArray();
        final int index = (l.length > 0) ? (int) l[0] : 0;
        i += mNumIndexBits;
        l = b.get(i, i + mNumTagBits).toLongArray();
        final int tag = (l.length > 0) ? (int) l[0] : 0;
        return new int[] { tag, index, offset };
    }

    public long cacheAddrToAddr(int tag, int index, int offset) {
        return (tag << mNumIndexBits + mNumOffsetBits) | (index << mNumOffsetBits) | offset;
    }

    @Override
    protected void update(long address, long[] values) {
        final int[] cacheAddr = addrToCacheAddr(address);
        final int tag = cacheAddr[0];
        final int index = cacheAddr[1];
        final int offset = cacheAddr[2];
        if (offset != 0) {
            throw new RuntimeException("Unaligned update");
        }
        CacheLine l = null;

        for (int b = 0; l == null && b < mBlocks.length; b++) {
            final CacheBlock block = mBlocks[b];
            final CacheLine line = block.mLines[index];
            if (line.mTag == tag) {
                block.setRecentlyUsed(index);
                l = line;
            }
        }
        if (l == null) {
            l = pickVictim(tag, index);
        }
        for (int i = 0; i < mLineSize; i++) {
            if (!l.mData.mDirty[i] || !l.mData.mValid[i]) {
                l.mData.writeValue(i, values[i]);
                l.mData.mDirty[i] = false;
            }
        }
        l.mRecentlyUsed = true;
    }

    private CacheLine pickVictim(int tag, int index) {
        final CacheLine[] ls = new CacheLine[mAssociativity];

        // get all the lines which can be used
        for (int i = 0; i < mAssociativity; i++) {
            ls[i] = mBlocks[i].mLines[index];
        }

        CacheLine l = null;

        // pick invalid line
        for (int i = 0; l == null && i < mAssociativity; i++) {
            if (ls[i].mData.validCount() == 0) {
                l = ls[i];
            }
        }

        if (l == null) {
            // pick least recently used
            for (int i = 0; i < mAssociativity; i++) {
                // if one has already been found,
                // replace it only if this one has less dirty values than the
                // previous
                if (!ls[i].mRecentlyUsed && (l == null || (l.mData.dirtyCount() > ls[i].mData.dirtyCount()))) {
                    l = ls[i];
                }
            }
        }

        if (l == null) {// if all of them are recently used, pick anyone
            l = mBlocks[0].mLines[index];
        }

        if (l.mData.dirtyCount() > 0) {
            lowerWrite(cacheAddrToAddr(l.mTag, index, 0), l.mData);
        }
        Arrays.fill(l.mData.mDirty, false);
        Arrays.fill(l.mData.mValid, false);
        l.mTag = tag;
        return l;
    }

    public void clear(long address) {
        final int[] cacheAddr = addrToCacheAddr(address);
        final int tag = cacheAddr[0];
        final int index = cacheAddr[1];
        final int offset = cacheAddr[2];
        CacheLine l;
        // no flush, invalidate
        for (int i = 0; i < mAssociativity; i++) {// for each set
            l = mBlocks[i].mLines[index];
            if (l.mTag == tag) {
                if (l.mData.mDirty[offset]) {
                    System.err.println("Cache clearing a value at addr " + address);
                }
                l.mData.mValid[offset] = false;
            }
        }
    }

    public boolean flush(long address) {
        final int[] cacheAddr = addrToCacheAddr(address);
        final int tag = cacheAddr[0];
        final int index = cacheAddr[1];
        CacheLine l;
        // flush, no invalidate
        boolean redo = false;
        for (int i = 0; !redo && i < mAssociativity; i++) {// for each set
            l = mBlocks[i].mLines[index];
            if (l.mData.dirtyCount() > 0 && l.mTag == tag) {
                lowerWrite(cacheAddrToAddr(tag, index, 0), l.mData);
                Arrays.fill(l.mData.mDirty, false);
                redo = true;
            }
        }
        if (!redo) {
            // writeback is only complete when the mWriteRequests is empty
            // and there are no pending writes
            return mWriteRequests.size() > 0;
        }
        return true;
    }
}
