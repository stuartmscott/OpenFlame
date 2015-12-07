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

import architecture.cache.ICache;
import architecture.decode.IDecoder;

public interface ICard {

    /**
     * @return the identifier representing this card.
     */
    int getId();

    /**
     * @return the motherboard in which this card is plugged.
     */
    IMotherboard getMotherboard();

    /**
     * @return the pipeline instructions flow through.
     */
    IPipeline getPipeline();

    /**
     * @return the decoder to be used to decode instructions.
     */
    IDecoder getDecoder();

    /**
     * @return the instruction cache.
     */
    ICache getInstructionStore();

    /**
     * @return the data cache.
     */
    ICache getDataStore();

    /**
     * @return the storage system below the caches.
     */
    ICache getLowerStore();

    /**
     * @return the signal the card is currently handling.
     */
    int getSignal();

    /**
     * Sets the signal the card is currently handling.
     *
     * @return true if the card wasn't already signaled.
     */
    boolean signal(int signal);

    /**
     * Clears the signal the card is currently handling.
     */
    void clearSignal();

    /**
     * @return the memory address where the interrupt handler lookup table is stored.
     */
    long getInterruptTableAddress();

    /**
     * Sets the memory address where the interrupt handler lookup table is stored.
     */
    void setInterruptTableAddress(long address);

}
