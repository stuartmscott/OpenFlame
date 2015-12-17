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
package architecture.decode;

import architecture.IContext;
import architecture.isa.Instruction;

public interface IDecoder {

    /**
     * Decodes an instruction for the given context.
     *
     * Instruction Format
     * 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000
     *
     * 1ppb cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc ---- --22 2222 - jump
     * 010o cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc 1111 1122 2222 - load/store
     * 011o mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm mmmm - push/pop
     * 001- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- - free/reserved
     * 0001 cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc cccc ---- --22 2222 - loadc
     * 0000 1ftt ttcc cccc cccc cccc cccc cccc cccc cccc cccc cc00 0000 1111 1122 2222 - alu
     * 0000 01tt ttcc cccc cccc cccc cccc cccc cccc cccc cccc cc-- ---- 1111 1122 2222 - special
     * 0000 001o ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- ---- --22 2222 - call/return
     *
     * @return the decoded instruction or an unsupported operation interrupt if the instruction
     * cannot be decoded.
     */
    Instruction decode(IContext context);
}
