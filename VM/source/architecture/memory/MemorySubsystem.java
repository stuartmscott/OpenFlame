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
package architecture.memory;

import architecture.Clockable;
import architecture.Motherboard;
import architecture.Utilities;

public abstract class MemorySubsystem extends Clockable implements IMemorySubsystem {

    public final int mBusSize;

    public volatile boolean mSuccess = false;
    public volatile boolean mBusy = false;
    public volatile boolean mFree = true;
    public volatile boolean mRead = false;
    public volatile boolean mWrite = false;
    public volatile long mAddress;
    private final DataLine mBus;

    public MemorySubsystem(int numClocks, int busSize) {
        super(numClocks);
        mBusSize = busSize;
        mBus = new DataLine(busSize);
    }

    public DataLine getBus() {
        return mBus;
    }

    public void clock() {
        if (isBusy()) {
            handleRequest();
            mRead = mWrite = false;
            mBusy = false;
        }
    }

    protected abstract void handleRequest();

    public synchronized void read(long address) {
        if (isBusy()) {
            throw new RuntimeException("Memory is already busy");
        } else if (address < 0 || address >= Motherboard.RAM_SIZE) {
            throw new RuntimeException("Memory access error");
        } else {
            Utilities.trace(Utilities.CACHE_COMMAND, "Cache read: " + address);
            mBusy = true;
            mAddress = address;
            mRead = true;
            mFree = false;
        }
    }

    public void write(long addr, long value) {
        DataLine data = new DataLine(mBusSize);
        data.writeValue(0, value);
        write(addr, data);
    }

    public synchronized void write(long address, DataLine data) {
        if (isBusy()) {
            throw new RuntimeException("Memory is already busy");
        } else if (address < 0 || address >= Motherboard.RAM_SIZE) {
            throw new RuntimeException("Memory access error");
        } else {
            Utilities.trace(Utilities.CACHE_COMMAND, "Cache write: " + address + " " + data);
            mBusy = true;
            mAddress = address;
            getBus().cloneFrom(data);
            mWrite = true;
            mFree = false;
        }
    }

    public boolean success() {
        return mSuccess;
    }

    public boolean isBusy() {
        return mBusy;
    }

    public boolean isFree() {
        return mFree;
    }

    public void free() {
        this.mFree = true;
    }

}
