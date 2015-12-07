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

import internalrep.asm.AsmStmt;
import io.Emittable;
import java.util.ArrayList;
import java.util.List;

public class Optimizer {

    private AsmStmt stmt;

    public Optimizer(AsmStmt stmt) {
        this.stmt = stmt;
    }

    public List<Emittable> reduce() {
        // TODO patterns to look for
        // register getting written twice without a read in between (or jump/call/ret/intr/iret)
        // copy where source and dest are the same
        // jump where target is next pc
        List<Emittable> stmts = new ArrayList<Emittable>();
        AsmStmt s = stmt;
        long address = 0;
        while (s != null) {
            s.setAddress(address);
            if (s instanceof Emittable) {
                stmts.add((Emittable) s);
                address++;
            }
            s = s.next;
        }
        return stmts;
    }

}
