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
package architecture.isa.special;

import architecture.IContext;

public class Halt extends Special {

    public Halt(IContext mContext) {
        super(mContext, "Halt");
    }

    public void load() {
        // Do Nothing
    }

    public void execute() {
        mCard.getMotherboard().halt();
    }

    public void store() {
        // Do Nothing
    }

    public void format() {
        // Do nothing
    }

    public void retire() {
        // Do Nothing
    }

}
