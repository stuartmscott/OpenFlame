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

import architecture.isa.Instruction;

public class DecodeNode {

    public final int mHeight;
    public volatile DecodeNode mLeft;
    public volatile DecodeNode mRight;

    public DecodeNode(int height, DecodeNode left, DecodeNode right) {
        mHeight = height;
        mLeft = left;
        mRight = right;
    }

    public DecodeNode() {
        this(-1, null, null);
    }

    public Instruction getInst(Decoder d) {
        if (d.mBits.get(mHeight)) {
            return mLeft.getInst(d);
        }
        return mRight.getInst(d);
    }

}
