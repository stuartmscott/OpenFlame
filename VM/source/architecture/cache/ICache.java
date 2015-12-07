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
package architecture.cache;

import architecture.memory.IMemorySubsystem;

public interface ICache extends IMemorySubsystem {

    /**
     * Makes this cache read only.
     */
    void setReadOnly();

    /**
     * Clears the value at the given address, next time it will be read in from main memory.
     */
    void clear(long address);

    /**
     * Flushes the value at the given address out to main memory.
     * @return true if the operation completed, false if it needs to be repeated next cycle.
     */
    boolean flush(long address);
}
