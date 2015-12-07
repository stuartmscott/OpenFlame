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
import org.mockito.Mockito;

import architecture.IMotherboard;

public class MemoryPortTest {

    public static final int BUS_SIZE = 2;
    public IMotherboard mMotherboard;
    public MemoryPort mMemoryPort;

    @Before
    public void setUp() {
        mMotherboard = Mockito.mock(IMotherboard.class);
        mMemoryPort = new MemoryPort(mMotherboard, BUS_SIZE);
    }

    @Test
    public void read() {
        Mockito.when(mMotherboard.read(12)).thenReturn((long) 34);
        Mockito.when(mMotherboard.read(13)).thenReturn((long) 43);
        mMemoryPort.read(12);
        mMemoryPort.clock();
        Assert.assertEquals(34, mMemoryPort.getBus().mValues[0]);
        Assert.assertEquals(43, mMemoryPort.getBus().mValues[1]);
        Assert.assertEquals(true, mMemoryPort.mSuccess);
    }

    @Test
    public void write() {
        mMemoryPort.write(44, 21);
        mMemoryPort.clock();
        mMemoryPort.write(45, 43);
        mMemoryPort.clock();
        Mockito.verify(mMotherboard).write(44, 21);
        Mockito.verify(mMotherboard).write(45, 43);
        Assert.assertEquals(true, mMemoryPort.mSuccess);
    }

}
