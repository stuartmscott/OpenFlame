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

import architecture.isa.special.Interrupt;

public class RegisterBank implements IRegisterBank {

    public final long[] mRegisters;
    public final ICard mCard;
    public final IContext mContext;
    public final int mNumGPRegs;

    public RegisterBank(IContext context, int numGPRegs) {
        mContext = context;
        mCard = context.getCard();
        mNumGPRegs = numGPRegs;
        mRegisters = new long[numGPRegs];
    }

    public long read(int register) {
        if (register < 0 || register >= mNumGPRegs) {
            mContext.error(Interrupt.REG_ACCESS_ERROR);
            return 0;
        }
        if (register == 0) {
            return 0;
        } else if (register == 1) {
            return 1;
        }
        long value = mRegisters[register];
        Utilities.trace(Utilities.REG_BANK, mCard.getId() + " : " + mContext.getId() + " RegBank read : " + register + " : " + value);
        return value;
    }

    public void write(int register, long value) {
        if (register < 0 || register >= mNumGPRegs || register == 0 || register == 1) {
            mContext.error(Interrupt.REG_ACCESS_ERROR);
            return;
        }
        Utilities.trace(Utilities.REG_BANK, mCard.getId() + " : " + mContext.getId() + " RegBank write : " + register + " : " + value);
        mRegisters[register] = value;
    }

    public void incrementProgramCounter() {
        mRegisters[2]++;
    }

}
