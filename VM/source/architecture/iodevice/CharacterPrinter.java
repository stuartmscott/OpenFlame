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
package architecture.iodevice;

import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import architecture.Motherboard;

public class CharacterPrinter extends IoDevice {

    public static final int DISPLAY_WIDTH = 80;

    public DisplayFrame mFrame;
    public StringBuffer mText = new StringBuffer();

    public CharacterPrinter(int id, Motherboard arch) {
        super(id, arch, 1);
    }

    protected boolean handleCommand() {
        int command = (int) ((mInput >> 32) & 0xFFFFFFFF);
        int parameter = (int) (mInput & 0xFFFFFFFF);
        switch (command) {
            case ON:
                mFrame = new DisplayFrame();
                mFrame.setVisible(true);
                mOutput = STATUS_OK;
                break;
            case OFF:
                mFrame.dispatchEvent(new WindowEvent(mFrame, WindowEvent.WINDOW_CLOSING));
                mOutput = STATUS_OK;
                break;
            case READ:
                mOutput = STATUS_ERROR;
                break;
            case WRITE:
                mText.append((char) parameter);
                mFrame.mArea.setText(mText.toString());
                mOutput = STATUS_OK;
                break;
        }
        return true;
    }

    public static class DisplayFrame extends JFrame {
        public static final long serialVersionUID = 1L;
        public final Dimension mSize = new Dimension(400, 300);
        public final JTextArea mArea = new JTextArea();
        public JScrollPane mScroll = new JScrollPane(mArea);

        public DisplayFrame() {
            super("Display");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            mScroll.setMaximumSize(mSize);
            mScroll.setMinimumSize(mSize);
            mScroll.setPreferredSize(mSize);
            mScroll.setSize(mSize);
            getContentPane().add(mScroll);
            mArea.setEditable(false);
            setResizable(false);
            pack();
            setLocationRelativeTo(null);
        }

    }

}
