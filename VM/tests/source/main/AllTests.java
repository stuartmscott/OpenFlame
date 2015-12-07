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
package main;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    architecture.RegisterBankTest.class,
    architecture.isa.arithmetic.AddTest.class,
    architecture.isa.arithmetic.ConvertTest.class,
    architecture.isa.arithmetic.DivideTest.class,
    architecture.isa.arithmetic.ModuloTest.class,
    architecture.isa.arithmetic.MultiplyTest.class,
    architecture.isa.arithmetic.SubtractTest.class,
    architecture.memory.DataLineTest.class,
    architecture.memory.MemoryPortTest.class
})
public class AllTests {
    //nothing
}
