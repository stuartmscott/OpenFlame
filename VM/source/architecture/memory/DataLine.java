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

public class DataLine {

    public final boolean[] mValid;
    public final boolean[] mDirty;
    public final long[] mValues;
    public final int mSize;

    public DataLine(int size) {
        mSize = size;
        mValid = new boolean[size];
        mDirty = new boolean[size];
        mValues = new long[size];
    }

    public void writeTo(DataLine data, int offset) {
        for (int i = 0; i < mSize; i++) {
            if (mDirty[i] && mValid[i]) {
                data.writeValue(i + offset, mValues[i]);
                mDirty[i] = false;
            }
        }
    }

    public void writeValue(int index, long value) {
        mValues[index] = value;
        mDirty[index] = true;
        mValid[index] = true;
    }

    public void cloneFrom(DataLine data) {
        for (int i = 0; i < mSize; i++) {
            mValid[i] = data.mValid[i];
            mDirty[i] = data.mDirty[i];
            mValues[i] = data.mValues[i];
        }
    }

    public int validCount() {
        int count = 0;
        for (int i = 0; i < mSize; i++) {
            if (mValid[i]) {
                count++;
            }
        }
        return count;
    }

    public int dirtyCount() {
        int count = 0;
        for (int i = 0; i < mSize; i++) {
            if (mDirty[i]) {
                count++;
            }
        }
        return count;
    }
}
