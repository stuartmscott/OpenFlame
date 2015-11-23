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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RegisterBankTest {

    private ICard mCard;
    private IContext mContext;
    private RegisterBank mRegisterBank;

    @Before
    public void setUp() {
        mCard = Mockito.mock(ICard.class);
        Mockito.when(mCard.getId()).thenReturn(0);
        mContext = Mockito.mock(IContext.class);
        Mockito.when(mContext.getId()).thenReturn(0);
        Mockito.when(mContext.getCard()).thenReturn(mCard);
        mRegisterBank = new RegisterBank(mContext, 10);
    }

    @Test
    public void read() {
        mRegisterBank.mRegisters[6] = 12;
        Assert.assertEquals("Incorrent answer", 12, mRegisterBank.read(6));
        Mockito.verify(mContext, Mockito.never()).error(2);
    }

    @Test
    public void write() {
        mRegisterBank.write(6, 12);
        Assert.assertEquals("Incorrent answer", 12, mRegisterBank.mRegisters[6]);
        Mockito.verify(mContext, Mockito.never()).error(2);
    }

    @Test
    public void incrementProgramCounter() {
        mRegisterBank.mRegisters[2] = 12;
        mRegisterBank.incrementProgramCounter();
        Assert.assertEquals("Incorrent answer", 13, mRegisterBank.read(2));
        Mockito.verify(mContext, Mockito.never()).error(2);
    }
}
