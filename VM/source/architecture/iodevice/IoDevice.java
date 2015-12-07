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
package architecture.iodevice;

import java.util.Arrays;

import architecture.Clockable;
import architecture.Motherboard;
import architecture.memory.MemoryPort;

public abstract class IoDevice extends Clockable {

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;

    public static final int ISSUE_READ = 0;
    public static final int LOAD_CMD = 1;
    public static final int EXEC_CMD = 2;
    public static final int WRITE_MEM = 3;
    public static final int RETIRE = 4;

    public static final int ON = 0;
    public static final int OFF = 1;
    public static final int READ = 2;
    public static final int WRITE = 3;

    public final Motherboard mArchitecture;
    public final MemoryPort mPort;
    public final int mId;

    public volatile int mSignal = -1;
    public int mStage = 0;
    public long mInput;
    public long mOutput;

    public IoDevice(int id, Motherboard architecture, int busSize) {
        super(100);
        mId = id;
        mArchitecture = architecture;
        mPort = new MemoryPort(architecture, busSize);
        mClockableTasks.add(mPort);
    }

    public void clock() {
        if (mSignal != -1) {
            switch (mStage) {
                case ISSUE_READ:
                    mPort.read(mId);
                    mStage++;
                    break;
                case LOAD_CMD:
                    mInput = mPort.getBus().mValues[0];
                    mOutput = 0;
                    mStage++;
                    break;
                case EXEC_CMD:
                    if (handleCommand())
                        mStage++;
                    break;
                case WRITE_MEM:
                    Arrays.fill(mPort.getBus().mDirty, false);
                    Arrays.fill(mPort.getBus().mValid, false);
                    mPort.getBus().writeValue(0, mOutput);
                    mPort.write(mId, mPort.getBus());
                    mStage++;
                    break;
                case RETIRE:
                    if (mArchitecture.signal(mId, mSignal)) {
                        mSignal = -1;
                        mStage = ISSUE_READ;
                    }
                    break;
            }
        }
        deviceClocked();
    }

    protected abstract boolean handleCommand();

    private void deviceClocked() {
        // Do nothing, some IO devices will override this.
    }

    public boolean signal(int id) {
        int old = mSignal;
        mSignal = id;
        return old == -1;
    }

}
