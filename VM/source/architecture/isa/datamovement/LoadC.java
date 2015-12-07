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
package architecture.isa.datamovement;

import architecture.IContext;
import architecture.isa.Instruction;

public class LoadC extends Instruction {

    public final long mConstant;
    public final int mDestinationRegister;

    public LoadC(IContext context, long constant, int destinationRegister) {
        super(context, "LoadC");
        this.mConstant = constant;
        this.mDestinationRegister = destinationRegister;
        trace("LoadC " + constant + " " + destinationRegister);
    }

    public void load() {
        mPipeline.setLoadRegister0(mConstant);
    }

    public void execute() {
        mPipeline.setExecuteRegister0(mPipeline.getLoadRegister0());
    }

    public void format() {
        mPipeline.setFormatRegister0(mPipeline.getExecuteRegister0());
    }

    public void store() {
        mContext.getRegisterBank().write(mDestinationRegister, mPipeline.getFormatRegister0());
    }

}
