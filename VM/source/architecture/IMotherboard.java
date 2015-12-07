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

public interface IMotherboard {

    /**
     * Halts the execution of the virtual machine.
     */
    void halt();

    /**
     * Reads the value from main memory at the given address.
     */
    long read(long address);

    /**
     * Writes the value to main memory at the given address.
     */
    void write(long address, long value);

    /**
     * Used by cards to signal other cards or io devices.
     *
     * Devices use mem[id] as their port.
     * A card must;
     *  - lock
     *  - check if target port holds zero
     *  - write new value
     *  - flush it
     *  - unlock
     *  - signal device
     * If the target port is not zero, the device has a pending signal
     *
     * @param sourceId the id of the signal's source
     * @param destinationId the id of the signal's destination
     * @return true if the destination was signaled.
     */
    boolean signal(int sourceId, int destinationId);
}
