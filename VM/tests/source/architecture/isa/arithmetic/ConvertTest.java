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
package architecture.isa.arithmetic;

import main.TestUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import architecture.ICard;
import architecture.IContext;
import architecture.Pipeline;
import architecture.RegisterBank;

public class ConvertTest {

    private ICard mCard;
    private Pipeline mPipeline;
    private IContext mContext;
    private RegisterBank mRegisterBank;

    @Before
    public void setUp() {
        mCard = Mockito.mock(ICard.class);
        Mockito.when(mCard.getId()).thenReturn(0);
        mPipeline = new Pipeline();
        Mockito.when(mCard.getPipeline()).thenReturn(mPipeline);
        mContext = Mockito.mock(IContext.class);
        Mockito.when(mContext.getId()).thenReturn(0);
        Mockito.when(mContext.getCard()).thenReturn(mCard);
        mRegisterBank = new RegisterBank(mContext, 10);
        Mockito.when(mContext.getRegisterBank()).thenReturn(mRegisterBank);
    }

    @Test
    public void convert_integer() {
        mRegisterBank.write(6, 12);
        Convert convert = new Convert(mContext, false, 6, 7, 8);
        TestUtilities.runInstruction(convert);
        Assert.assertEquals("Incorrent answer", Double.doubleToLongBits(12.0), mRegisterBank.read(8));
        Mockito.verify(mContext, Mockito.never()).error(Mockito.anyLong());
    }

    @Test
    public void convert_float() {
        mRegisterBank.write(6, Double.doubleToLongBits(12.0));
        Convert convert = new Convert(mContext, true, 6, 7, 8);
        TestUtilities.runInstruction(convert);
        Assert.assertEquals("Incorrent answer", 12, mRegisterBank.read(8));
        Mockito.verify(mContext, Mockito.never()).error(Mockito.anyLong());
    }
}
