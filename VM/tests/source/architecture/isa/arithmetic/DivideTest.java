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

public class DivideTest {

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
    public void divide_integer() {
        mRegisterBank.write(6, 1);
        mRegisterBank.write(7, 2);
        Divide divide = new Divide(mContext, false, 6, 7, 8);
        TestUtilities.runInstruction(divide);
        Assert.assertEquals("Incorrent answer", 0, mRegisterBank.read(8));
        Mockito.verify(mContext, Mockito.never()).error(Mockito.anyLong());
    }

    @Test
    public void divide_float() {
        mRegisterBank.write(6, Double.doubleToLongBits(1));
        mRegisterBank.write(7, Double.doubleToLongBits(2));
        Divide divide = new Divide(mContext, true, 6, 7, 8);
        TestUtilities.runInstruction(divide);
        Assert.assertEquals("Incorrent answer", Double.doubleToLongBits(0.5), mRegisterBank.read(8), 0.01);
        Mockito.verify(mContext, Mockito.never()).error(Mockito.anyLong());
    }

    @Test
    public void divide_integer_zero() {
        mRegisterBank.write(6, 64);
        mRegisterBank.write(7, 0);
        Divide divide = new Divide(mContext, false, 6, 7, 8);
        TestUtilities.runInstruction(divide);
        Mockito.verify(mContext, Mockito.times(1)).error(Mockito.anyLong());
    }

    @Test
    public void divide_float_zero() {
        mRegisterBank.write(6, Double.doubleToLongBits(87));
        mRegisterBank.write(7, Double.doubleToLongBits(0.0));
        Divide divide = new Divide(mContext, true, 6, 7, 8);
        TestUtilities.runInstruction(divide);
        Mockito.verify(mContext, Mockito.times(1)).error(Mockito.anyLong());
    }
}
