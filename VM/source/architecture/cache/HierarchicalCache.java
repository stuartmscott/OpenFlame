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

import java.util.LinkedList;

import architecture.Utilities;
import architecture.memory.DataLine;
import architecture.memory.MemorySubsystem;

public abstract class HierarchicalCache extends MemorySubsystem implements ICache {

    //Sizes are in number 64bit block
    //Level		bus size	line size
    //reg		n/a			1
    //l1		1			8
    //l2		8			64
    //mem port	64			n/a

    public final int mLineSize;
    public final MemorySubsystem mLower;
    public final LinkedList<Long> mReadRequests = new LinkedList<Long>();
    public final LinkedList<Long> mWriteRequests = new LinkedList<Long>();
    public final LinkedList<DataLine> mWriteData = new LinkedList<DataLine>();
    public volatile boolean mReadPending = false;
    public volatile boolean mWritePending = false;

    public HierarchicalCache(int numClocks, int busSize, int lineSize, MemorySubsystem lower) {
        super(numClocks, busSize);
        mLineSize = lineSize;
        mLower = lower;
        //TODO in general, get rid of the linked list and use a circular buffer
    }

    public void clock() {
        if (!mLower.isBusy()) {
            if (mReadPending) {
                mReadPending = false;
                mLower.free();
                if (mLower.success()) {
                    final long address = mReadRequests.remove();
                    Utilities.trace(Utilities.CACHE_HIERARCHY, "Completed read from lower level: " + address);
                    update(address, mLower.getBus().mValues);
                }
            } else if (mWritePending) {
                mWritePending = false;
                mLower.free();
                if (mLower.success()) {
                    final long address = mWriteRequests.remove();
                    mWriteData.remove();
                    Utilities.trace(Utilities.CACHE_HIERARCHY, "Completed write to lower level: " + address);
                }
            } else if (mLower.isFree()){
                if (!mReadRequests.isEmpty()) {
                    final long address = mReadRequests.element();
                    final int index = mWriteRequests.indexOf(address);
                    if (index == -1) {
                        Utilities.trace(Utilities.CACHE_HIERARCHY, "Issuing read to lower level: " + address);
                        mLower.read(address);
                        mReadPending = true;
                    } else {
                        Utilities.trace(Utilities.CACHE_HIERARCHY, "Completed read request using writebuffer: " + address);
                        update(address, mWriteData.get(index).mValues);
                        mReadRequests.remove();
                    }
                } else if (!mWriteRequests.isEmpty()) {
                    final long address = mWriteRequests.element();
                    DataLine data = mWriteData.element();
                    Utilities.trace(Utilities.CACHE_HIERARCHY, "Issuing write to lower level: " + address);
                    mLower.write(address, data);
                    mWritePending = true;
                }
            }
        }
        super.clock();
    }

    protected abstract void update(long address, long[] values);

    protected void lowerRead(long address) {
        Utilities.trace(Utilities.CACHE_HIERARCHY, "Lower read: " + address);
        if (!mReadRequests.contains(address)) {
            mReadRequests.add(address);
        }
    }

    protected void lowerWrite(long address, DataLine data) {
        Utilities.trace(Utilities.CACHE_HIERARCHY, "Lower write: " + address + " " + data);
        final int index = mWriteRequests.indexOf(address);
        if ((mWritePending && index == 0) || index == -1) {//a write of value in queue at 0 has already been started, make a new elem in queue
            mWriteRequests.add(address);
            DataLine newData = new DataLine(data.mSize);
            newData.cloneFrom(data);
            mWriteData.add(newData);
        } else {
            data.writeTo(mWriteData.get(index), 0);
        }
    }

}
