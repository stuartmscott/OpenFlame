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
package optimizer;

import internalrep.assembly.AssemblyStatement;
import io.Emittable;
import java.util.ArrayList;
import java.util.List;

public class Optimizer {

    private AssemblyStatement mStatement;

    public Optimizer(AssemblyStatement statement) {
        mStatement = statement;
    }

    public List<Emittable> reduce() {
        // TODO patterns to look for
        // register getting written twice without a read in between (or jump/call/ret/intr/iret)
        // copy where source and destination are the same
        // jump where target is next pc
        List<Emittable> statements = new ArrayList<Emittable>();
        AssemblyStatement statement = mStatement;
        long address = 0;
        while (statement != null) {
            statement.setAddress(address);
            if (statement instanceof Emittable) {
                statements.add((Emittable) statement);
                address++;
            }
            statement = statement.mNext;
        }
        return statements;
    }

}
