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

public class Pipeline implements IPipeline {

    public volatile long mLoadRegister0;
    public volatile long mLoadRegister1;
    public volatile long mExecuteRegister0;
    public volatile long mExecuteRegister1;
    public volatile long mFormatRegister0;
    public volatile long mFormatRegister1;

    @Override
    public long getLoadRegister0() {
        return mLoadRegister0;
    }

    @Override
    public void setLoadRegister0(long value) {
        mLoadRegister0 = value;
    }

    @Override
    public long getLoadRegister1() {
        return mLoadRegister1;
    }

    @Override
    public void setLoadRegister1(long value) {
        mLoadRegister1 = value;
    }

    @Override
    public long getExecuteRegister0() {
        return mExecuteRegister0;
    }

    @Override
    public void setExecuteRegister0(long value) {
        mExecuteRegister0 = value;
    }

    @Override
    public long getExecuteRegister1() {
        return mExecuteRegister1;
    }

    @Override
    public void setExecuteRegister1(long value) {
        mExecuteRegister1 = value;
    }

    @Override
    public long getFormatRegister0() {
        return mFormatRegister0;
    }

    @Override
    public void setFormatRegister0(long value) {
        mFormatRegister0 = value;
    }

    @Override
    public long getFormatRegister1() {
        return mFormatRegister1;
    }

    @Override
    public void setFormatRegister1(long value) {
        mFormatRegister1 = value;
    }
}
