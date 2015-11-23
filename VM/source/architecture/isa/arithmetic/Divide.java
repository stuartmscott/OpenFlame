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
import architecture.isa.special.Interrupt;

public class Divide extends AluInst {

    public Divide(IContext context, boolean floatingPoint, int source1, int source2, int destination) {
        super(context, floatingPoint, source1, source2, destination, "Div");
        trace("Div " + source1 + " " + source2 + " " + destination);
    }

    public void execute() {
        if (mPipeline.getLoadRegister1() == 0) {
            mContext.error(Interrupt.ARITHMETIC_ERROR);
        } else if (mFloatingPoint) {
            double load0 = Double.longBitsToDouble(mPipeline.getLoadRegister0());
            double load1 = Double.longBitsToDouble(mPipeline.getLoadRegister1());
            mPipeline.setExecuteRegister0(Double.doubleToLongBits(load0 / load1));
        } else {
            mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0() / mPipeline.getLoadRegister1());
        }
    }

}
