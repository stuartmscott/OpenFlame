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

public interface IContext {

    /**
     * @return the identifier representing this context.
     */
    int getId();

    /**
     * @return the card on which this context is running.
     */
    ICard getCard();

    /**
     * @return this context's bank of registers.
     */
    IRegisterBank getRegisterBank();

    /**
     * @return the identifier of the device which signaled this context.
     */
    long getDeviceId();

    /**
     * @return the identifier of the error that this context is handling.
     */
    long getErrorId();

    /**
     * @return the address of the next instruction.
     */
    long getProgramCounter();

    /**
     * @return the instruction the context is currently performing.
     */
    long getInstruction();

    /**
     * @return true is the instruction is being retried.
     */
    boolean isRetry();

    /**
     * Set whether this instruction will be retried.
     */
    void setRetry(boolean retry);

    /**
     * Puts this context to sleep. An interrupt can wake it again.
     */
    void sleep();

    /**
     * Triggers an error in the context to be caught by an interrupt handler.
     *
     * @param error A representing the type of error {@link Interrupt}
     */
    void error(long error);

    /**
     * @return true if the context is handling an interrupt.
     */
    boolean isHandlingInterrupt();

    /**
     * Sets the flag holding whether the context is in an interrupt.
     */
    void setIsHandlingInterrupt(boolean isHandling);

    /**
     * @return true if the context wants to get the hardware lock.
     */
    boolean requiresLock();

    /**
     * Sets whether the context wants to get the hardware lock.
     */
    void setRequiresLock(boolean required);

    /**
     * @return true if the context holds the hardware lock.
     */
    boolean hasLock();

    /**
     * Sets whether the context holds the hardware lock.
     */
    void setHasLock(boolean hasLock);

    /**
     * @return the address of the process instance, also its ID.
     */
    long getProcessId();

    /**
     * Set the address of the process instance, also its ID.
     */
    void setProcessId(long processId);

    /**
     * @return the address where code for the process currently executing on this context, starts.
     */
    long getCodeBase();

    /**
     * Set the address where code for the process currently executing on this context, starts.
     */
    void setCodeBase(long codeBase);

    /**
     * @return the address where the stack starts.
     */
    long getStackStart();

    /**
     * Sets the address where the stack starts.
     */
    void setStackStart(long address);

    /**
     * @return the address where the stack ends.
     */
    long getStackLimit();

    /**
     * Sets the address where the stack ends.
     */
    void setStackLimit(long address);

    /**
     * Stage 1: issue a fetch for the next instruction.
     */
    void fetchInst();

    /**
     * Stage 2: loads the next instruction from the cache.
     */
    void loadInstruction();

    /**
     * Stage 3: decodes the instruction.
     */
    void decode();

    /**
     * Stage 4: load's the instruction's parameters.
     */
    void load();

    /**
     * Stage 5: executes the instruction.
     */
    void execute();

    /**
     * Stage 6: formats the instruction's result.
     */
    void format();

    /**
     * Stage 7: stores the instruction's result.
     */
    void store();

    /**
     * Stage 8: retires the instruction, calculating the next instruction's address.
     */
    void retire();

}
