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

import java.util.ArrayList;
import java.util.concurrent.Callable;

public abstract class Clockable implements Callable<Integer> {

    public volatile boolean mRunning = true;
    public final int mNumClocks;
    public volatile int mDelay = 0;

    public final ArrayList<Clockable> mClockableTasks = new ArrayList<Clockable>();

    public Clockable(int numClocks) {
        mNumClocks = numClocks;
    }

    public Integer call() {
        try {
            for(Clockable c : mClockableTasks) {
                c.call();
            }
            if (mRunning) {
                mDelay++;
                if (mDelay >= mNumClocks) {
                    clock();
                    mDelay = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public abstract void clock();
}
