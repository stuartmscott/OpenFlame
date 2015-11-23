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

import architecture.IContext;

public class Modulo extends AluInst {

    public Modulo(IContext context, boolean floatingPoint, int sourceRegister1, int sourceRegister2, int destinationRegister) {
        super(context, floatingPoint, sourceRegister1, sourceRegister2, destinationRegister, "Mod");
        trace("Mod " + sourceRegister1 + " " + sourceRegister2 + " " + destinationRegister);
    }

    public void execute() {
        if (mFloatingPoint) {
            double load0 = Double.longBitsToDouble(mPipeline.getLoadRegister0());
            double load1 = Double.longBitsToDouble(mPipeline.getLoadRegister1());
            mPipeline.setExecuteRegister0(Double.doubleToLongBits(load0 % load1));
        } else {
            mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0() % mPipeline.getLoadRegister1());
            // Java issue only
            if (mPipeline.getExecuteRegister0() < 0) {
                mPipeline.setExecuteRegister0(mPipeline.getExecuteRegister0() + mPipeline.getLoadRegister0());
            }
        }
    }

}
