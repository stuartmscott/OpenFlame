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
package internalrep.assembly;

import internalrep.assembly.controlflow.Label;
import io.Emittable;
import linker.Linkable;
import linker.Linker;

public class Data extends AssemblyStatement implements Emittable, Linkable {

    public long mValue;
    public Label mLabel;
    private String mName;

    public Data(long value, String comment) {
        super(comment);
        mValue = value;
    }

    public Data(Label label, String comment) {
        super(comment);
        mLabel = label;
    }

    public Data(String name, String comment) {
        super(comment);
        mName = name;
    }

    public String toString() {
        if (mLabel != null) {
            return "data " + mLabel.mName + super.toString();
        }
        return "data " + mValue + super.toString() + " // \'" + (char) (mValue + '\0') + "\' : "+ Long.toHexString(mValue) + " : " + Long.toBinaryString(mValue);
    }

    public void link(Linker l) {
        if (mName != null) {
            mLabel = l.getLabel(mName);
        }
    }

    public long emit() {
        toValue();
        return mValue;
    }

    private void toValue() {
        if (mLabel != null) {
            mValue = mLabel.mAddress;
        }
    }

    public boolean shouldBeEmitted() {
        return true;
    }

}
