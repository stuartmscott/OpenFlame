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
package internalrep.assembly.controlflow;

import generator.Register;
import internalrep.assembly.AssemblyStatement;

public class Call extends AssemblyStatement {

    private Register mAddress;
    private int mAddressIndex;

    public Call(String comment, Register address) {
        super(comment);
        mAddress = address;
        mIsRegister = true;
        // calls the function at address
        // if the context isn't handling an interrupt, this adds the context codeBase to the address
    }

    public Call(String comment, int addressIndex) {
        super(comment);
        mAddressIndex = addressIndex;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mAddressIndex = mAddress.getHardwareRegister();
        }
    }

    public long emit() {
        // 0000 0011 reg
        toIndexies();
        return (3L << 56) | mAddressIndex;
    }

    public boolean shouldBeEmitted() {
        return true;
    }

    public String toString() {
        toIndexies();
        return "call r" + mAddressIndex + super.toString();
    }

}
