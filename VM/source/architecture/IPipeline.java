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

public interface IPipeline {

    /**
     * @return the value stored in the first load register.
     */
    long getLoadRegister0();

    /**
     * Sets the first load register to the given value.
     */
    void setLoadRegister0(long value);

    /**
     * @return the value stored in the second load register.
     */
    long getLoadRegister1();

    /**
     * Sets the second load register to the given value.
     */
    void setLoadRegister1(long value);

    /**
     * @return the value stored in the first execute register.
     */
    long getExecuteRegister0();

    /**
     * Sets the first execute register to the given value.
     */
    void setExecuteRegister0(long value);

    /**
     * @return the value stored in the second execute register.
     */
    long getExecuteRegister1();

    /**
     * Sets the second execute register to the given value.
     */
    void setExecuteRegister1(long value);

    /**
     * @return the value stored in the first format register.
     */
    long getFormatRegister0();

    /**
     * Sets the first format register to the given value.
     */
    void setFormatRegister0(long value);

    /**
     * @return the value stored in the second format register.
     */
    long getFormatRegister1();

    /**
     * Sets the second format register to the given value.
     */
    void setFormatRegister1(long value);
}
