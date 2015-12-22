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
package internalrep.assembly.special;

public class InterruptReturn extends Special {

    private int mParameterRegisterIndex;

    public InterruptReturn(String comment, int parameterRegisterIndex) {
        super(comment, INTERRUPT_RETURN);
        mParameterRegisterIndex = parameterRegisterIndex;
    }

    public long emit() {
        return super.emit() | mParameterRegisterIndex;
    }

    public String toString() {
        return "iret r" + mParameterRegisterIndex + super.toString();
    }

}
