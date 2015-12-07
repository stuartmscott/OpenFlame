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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import architecture.Motherboard;

public class StorageDrive extends IoDevice {

    public static final int BUS_SIZE = 8;

    public static final int SET_CONTROL_BLOCK = 16;

    public final Path mFile;
    public volatile FileChannel mChannel;
    public final ByteBuffer mBuffer = ByteBuffer.allocate(8);

    // Control block size limited to 8 longs
    public long mControlBlockAddr = 0;
    public long mDeviceAddress;
    public long mMemoryAddress;
    public long mCount;
    public long mIndex;

    public int mStage = 0;

    public StorageDrive(int id, Motherboard architecture, String filename) {
        super(id, architecture, BUS_SIZE);
        mFile = Paths.get(filename);
    }

    protected boolean handleCommand() {
        boolean success = true;
        boolean complete = true;
        int command = (int) ((mInput >> 32) & 0xFFFFFFFF);
        int param = (int) (mInput & 0xFFFFFFFF);
        switch (command) {
            case ON:
                try {
                    mChannel = FileChannel.open(mFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
                    mOutput = STATUS_OK;
                } catch (IOException e) {
                    System.err.println(e);
                    mOutput = STATUS_ERROR;
                }
                break;
            case OFF:
                try {
                    mChannel.close();
                    mOutput = STATUS_OK;
                } catch (IOException e) {
                    mOutput = STATUS_ERROR;
                }
                break;
            case READ:
                if (mStage == 0) {
                    mPort.read(mControlBlockAddr);
                    mStage++;
                } else if (mStage == 1) {
                    mDeviceAddress = mPort.getBus().mValues[0];
                    mMemoryAddress = mPort.getBus().mValues[1];
                    mCount = mPort.getBus().mValues[2];
                    mStage++;
                } else if (mStage == 2) {
                    try {
                        Arrays.fill(mPort.getBus().mDirty, false);
                        Arrays.fill(mPort.getBus().mValid, false);
                        mChannel.position((mDeviceAddress + mIndex) * 8);
                        long num = Math.min(mCount - mIndex, BUS_SIZE);
                        for (int i = 0; success && i < num; i++) {
                            mBuffer.rewind();
                            success = (mChannel.read(mBuffer) == 8);
                            mBuffer.rewind();
                            if (success)
                                mPort.getBus().writeValue(i, mBuffer.getLong());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!success) {
                        mOutput = STATUS_ERROR;
                        System.err.println("Storage device read failed");
                    } else {
                        mOutput = STATUS_OK;
                    }
                    mStage++;
                } else {
                    mPort.write(mMemoryAddress + mIndex, mPort.getBus());
                    mIndex += Math.min(mCount - mIndex, BUS_SIZE);
                    mStage = 0;
                }
                if (mIndex < mCount) {
                    complete = false;
                } else if (mStage != 0) {
                    complete = false;
                } else {
                    mIndex = 0;
                }
                break;
            case WRITE:
                if (mStage == 0) {
                    mPort.read(mControlBlockAddr);
                    mStage++;
                } else if (mStage == 1) {
                    mDeviceAddress = mPort.getBus().mValues[0];
                    mMemoryAddress = mPort.getBus().mValues[1];
                    mCount = mPort.getBus().mValues[2];
                    mStage++;
                } else if (mStage == 2) {
                    mPort.read(mMemoryAddress + mIndex);
                    mStage++;
                } else {
                    try {
                        mChannel.position((mDeviceAddress + mIndex) * 8);
                        long num = Math.min(mCount - mIndex, BUS_SIZE);
                        for (int i = 0; success && i < num; i++) {
                            mBuffer.rewind();
                            mBuffer.putLong(0, mPort.getBus().mValues[i]);
                            mBuffer.rewind();
                            success = (mChannel.write(mBuffer) == 8);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!success) {
                        mOutput = STATUS_ERROR;
                        System.err.println("Storage device write failed");
                    } else {
                        mOutput = STATUS_OK;
                    }
                    mIndex += Math.min(mCount - mIndex, BUS_SIZE);
                    mStage = 0;
                }
                if (mIndex < mCount) {
                    complete = false;
                } else if (mStage != 0) {
                    complete = false;
                } else {
                    mIndex = 0;
                }
                break;
            case SET_CONTROL_BLOCK:
                mControlBlockAddr = param;
                break;
        }
        return complete;
    }

}
