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
package generator;

import java.util.ArrayList;

public class RegisterMask {

    public ArrayList<Register> mRegisters;
    public long mMask = -1;

    public RegisterMask(Generator g, Register register) {
        this(g);
        if (register == null) {
            throw new RuntimeException();
        }
        mRegisters = new ArrayList<Register>();
        mRegisters.add(register);
    }

    public RegisterMask(Generator g, ArrayList<Register> registers) {
        this(g);
        mRegisters = registers;
    }

    private RegisterMask(Generator g) {
        if (g != null) {
            g.addMask(this);
        }
    }

    public long getMask() {
        if (mMask == -1) {
            boolean[] wasUsed = new boolean[Generator.GENERAL_PURPOSE_REGISTER_COUNT];
            for (Register ureg : mRegisters) {
                wasUsed[(Generator.GENERAL_PURPOSE_REGISTER_COUNT - 1) - ureg.getHardwareRegister()] = true;
            }
            mMask = Generator.getMask(wasUsed);
        }
        return mMask;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Register register : mRegisters) {
            sb.append(" r" + register.getHardwareRegister());
        }
        return sb.toString();
    }

}
