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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import architecture.iodevice.IoDevice;
import architecture.isa.Instruction;

public class Motherboard extends Thread implements IMotherboard {

    public static final int NUM_CARDS = 8;
    public static final int NUM_DEVICES = 8;

    public static final int BOOTLOADER_SIZE = 128;
    // (2^48)*8 bytes is the maximum memory that can be addressed by a 48bit
    // value which is how big the LOADC inst constant is
    public static final int RAM_SIZE = (int) Math.pow(2, 14); // 24

    public final Card[] mCards;
    public final IoDevice[] mIoDevices;
    public final long[] mRam = new long[RAM_SIZE];

    public volatile boolean mRunning = true;

    public volatile int mPrevious = 0;
    public volatile int mLockHolder = -1;
    public long mNumCycles = 0;

    public Motherboard(Card[] cards, IoDevice[] devices, String program) {
        mCards = cards;
        mIoDevices = devices;
        // Load Program into RAM
        try {
            final ByteBuffer buf = ByteBuffer.allocate(8);
            final Path file = Paths.get(program);
            final FileChannel fc = FileChannel.open(file, StandardOpenOption.READ);
            for (int index = 0, bytes = 0; bytes < fc.size(); index++, bytes+=8) {
                fc.position(index * 8);
                buf.clear();
                fc.read(buf);
                buf.rewind();
                // Write the value into RAM.
                mRam[index] = buf.getLong();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        final ArrayList<Clockable> clockableTasks = new ArrayList<Clockable>();
        for (Card c : mCards) {
            if (c != null) {
                clockableTasks.add(c);
            }
        }
        for (IoDevice d : mIoDevices) {
            if (d != null) {
                clockableTasks.add(d);
            }
        }

        final int numTasks = clockableTasks.size();
        final ExecutorService executionService = Executors.newFixedThreadPool(numTasks);
        final ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executionService);
        while (mRunning) {
            if (mLockHolder == -1) {
                int index;
                for (int i = 0; mLockHolder == -1 && i < mCards.length; i++) {
                    index = (i + mPrevious) % NUM_CARDS;
                    if (mCards[index].mRequiresLock) {
                        mCards[index].mHasLock = true;
                        mLockHolder = index;
                    }
                }
            } else {
                if (!mCards[mLockHolder].mRequiresLock) {
                    mCards[mLockHolder].mHasLock = false;
                    mPrevious = mLockHolder;
                    mLockHolder = -1;
                }
            }
            for (int i = 0; i < numTasks; i++) {
                completionService.submit(clockableTasks.get(i));
            }
            try {
                for (int i = 0; i < numTasks; i++)
                    completionService.take();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mNumCycles++;
        }
        executionService.shutdown();
    }

    public long read(long address) {
        return mRam[checkAddr(address)];
    }

    public void write(long address, long value) {
        mRam[checkAddr(address)] = value;
    }

    public boolean signal(int sourceId, int destinationId) {
        boolean success;
        if (destinationId < NUM_CARDS) {
            success = mCards[destinationId].signal(sourceId);
        } else {
            success = mIoDevices[destinationId - NUM_CARDS].signal(sourceId);
        }
        Utilities.trace(Utilities.ARCH_EVENT, sourceId + " signaling " + destinationId + ":" + ((success) ? "success" : "fail"));
        return success;
    }

    private int checkAddr(long address) {
        int a = (int) address;
        if (a != address) // This is just for the simulator (Java array size limit)
            System.err.println("Address larger than 32 bits");
        return a;
    }

    public void halt() {
        mRunning = false;
        for (int i = 0; i < mCards.length; i++) {
            mCards[i].mRunning = false;
        }
        Utilities.trace(Utilities.ARCH_EVENT, "Halted");
        Utilities.trace(Utilities.ARCH_EVENT, "Num Cycles: " + mNumCycles);
        Utilities.trace(Utilities.ARCH_EVENT, "Num Insts Decoded: " + Instruction.sInstructionsDecoded);
        HashMap<String, Integer> counts = Instruction.sInstructionCount;
        ArrayList<String> decodedInstructions = new ArrayList<String>();
        Set<String> keys = counts.keySet();
        for (String s : keys) {
            String value = counts.get(s).toString();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6 - value.length(); i++) {
                sb.append(0);
            }
            sb.append(value + " " + s);
            decodedInstructions.add(sb.toString());
        }
        Collections.sort(decodedInstructions);
        for (String s : decodedInstructions) {
            Utilities.trace(Utilities.ARCH_EVENT, s);
        }
        Utilities.flush();
    }

}
