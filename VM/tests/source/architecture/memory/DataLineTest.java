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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataLineTest {

    private DataLine mDataLine;

    @Before
    public void setUp() {
        mDataLine = new DataLine(4);
    }

    @Test
    public void writeTo() {
        mDataLine.mValid[1] = true;
        mDataLine.mDirty[1] = true;
        mDataLine.mValues[1] = 1234;
        DataLine dest = new DataLine(4);
        mDataLine.writeTo(dest, 1);// offset by 1
        Assert.assertEquals(true, dest.mValid[2]);
        Assert.assertEquals(true, dest.mDirty[2]);
        Assert.assertEquals(1234, dest.mValues[2]);
        // data was written so no longer dirty.
        Assert.assertEquals(false, mDataLine.mDirty[1]);
    }

    @Test
    public void writeValue() {
        mDataLine.writeValue(2, 1234);
        Assert.assertEquals(true, mDataLine.mValid[2]);
        Assert.assertEquals(true, mDataLine.mDirty[2]);
        Assert.assertEquals(1234, mDataLine.mValues[2]);
    }

    @Test
    public void validCount() {
        mDataLine.mValid[0] = true;
        mDataLine.mValid[1] = true;
        Assert.assertEquals(2, mDataLine.validCount());
    }

    @Test
    public void dirtyCount() {
        mDataLine.mDirty[1] = true;
        mDataLine.mDirty[2] = true;
        mDataLine.mDirty[3] = true;
        Assert.assertEquals(3, mDataLine.dirtyCount());
    }
}
