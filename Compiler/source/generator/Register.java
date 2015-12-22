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

import main.Compiler;

public class Register {

    public int mId = Generator.ANY_REGISTER;
    int mAllocationIndex = -1;// When it was allocated
    int mFreeIndex = -1;// When it was freed
    private ArrayList<Register> mLinkedRegs = new ArrayList<Register>();
    int mNumControllersWaiting = 0;
    final String mName;
    final boolean mIsExpression;
    boolean[] mAvailable;

    public Register(String name, boolean isExpression) {
        mName = name;
        mIsExpression = isExpression;
    }

    void controls(Register register) {
        mLinkedRegs.add(register);
        register.mNumControllersWaiting++;
    }

    void reduce() {
        if (mId != Generator.ANY_REGISTER) {
            return;// Already picked a hardware register
        }
        if (mNumControllersWaiting > 0) {
            return;//Need to let controllers pick first
        }
        for (int i = 0; i < Generator.GENERAL_PURPOSE_REGISTER_COUNT; i++) {
            if (mAvailable[i]) {
                mId = i;
                for (Register r : mLinkedRegs) {
                    r.mNumControllersWaiting--;
                    r.cannotTake(i, mFreeIndex);
                }
                //Successfully found a hardware register
                return;
            }
        }
        Compiler.error("could not allocate registers");
    }

    private void cannotTake(int id, int until) {
        if (until <= mAllocationIndex) {
            Compiler.error("Shouldn't be controlling");
        }
        mAvailable[id] = false;
    }

    public String toString() {
        if (mId != Generator.ANY_REGISTER) {
            return mName + ": r" + mId;
        }
        return mName;
    }

    public int getHardwareRegister() {
        if (mId == Generator.ANY_REGISTER) {
            Compiler.error("register " + this + " was not allocated");
        }
        return mId;
    }

}
