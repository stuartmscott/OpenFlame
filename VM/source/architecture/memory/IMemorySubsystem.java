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
package architecture.memory;

public interface IMemorySubsystem {

    DataLine getBus();

    /**
     * Issues a read request for the value at the address.
     */
    void read(long address);

    /**
     * Issues a write request for the given value to the address.
     */
    void write(long address, long value);

    /**
     * @return true if the subsystem is still handling a request.
     */
    boolean isBusy();

    /**
     * @return true if the request was successful.
     */
    boolean success();
}
