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

public class Sleep extends Special {

    public Sleep(IContext mContext) {
        super(mContext, "Sleep");
    }

    public void load() {
        // Do Nothing
    }

    public void execute() {
        mContext.sleep();
    }

    public void format() {
        // Do nothing
    }

    public void store() {
        // Do Nothing
    }

    public void retire() {
        // Do Nothing
    }

}
