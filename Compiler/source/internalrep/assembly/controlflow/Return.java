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

public class Return extends AssemblyStatement {

    private Register mAddress;
    private int mAddressIndex;

    public Return(String comment, Register address) {
        super(comment);
        mAddress = address;
        mIsRegister = true;
        // sets the pc to the value in mAddress
    }

    public Return(String comment, int addressIndex) {
        super(comment);
        mAddressIndex = addressIndex;
    }

    protected void toIndexies() {
        if (mIsRegister) {
            mAddressIndex = mAddress.getHardwareRegister();
        }
    }

    public long emit() {
        // 0000 0010 reg
        toIndexies();
        return (2L << 56) | mAddressIndex;
    }

    public boolean shouldBeEmitted() {
        return true;
    }

    public String toString() {
        toIndexies();
        return "ret r" + mAddressIndex + super.toString();
    }

}
