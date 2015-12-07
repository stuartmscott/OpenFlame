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
package architecture.isa;

import java.util.HashMap;

import architecture.ICard;
import architecture.IContext;
import architecture.IPipeline;
import architecture.Utilities;

public abstract class Instruction {

    public static final HashMap<String, Integer> sInstructionCount = new HashMap<String, Integer>();
    public static long sInstructionsDecoded = 0;

    public final ICard mCard;
    public final IPipeline mPipeline;
    public final IContext mContext;
    public volatile boolean mIsRetry = false;
    public volatile boolean mIncrementProgramCounter = true;

    public Instruction(IContext context, String instructionName) {
        mCard = context.getCard();
        mPipeline = mCard.getPipeline();
        mContext = context;
        int val = 0;
        if (sInstructionCount.containsKey(instructionName)) {
            val = sInstructionCount.get(instructionName);
        }
        val++;// Increment number of instName instructions decoded
        sInstructionCount.put(instructionName, val);
        sInstructionsDecoded++;// Increment total instructions decoded
    }

    protected void trace(String value) {
        Utilities.trace(Utilities.INSTRUCTION_DECODED, mCard.getId() + " : " + mContext.getId()
                + " : " + mContext.getProgramCounter() + " : " + value);
    }

    public abstract void load();

    public abstract void execute();

    public abstract void format();

    public abstract void store();

    public void retire() {
        if (!mContext.isRetry() && mIncrementProgramCounter) {
            mContext.getRegisterBank().incrementProgramCounter();
        }
    }
}
