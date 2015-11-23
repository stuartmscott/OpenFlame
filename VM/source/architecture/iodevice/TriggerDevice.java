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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import architecture.Motherboard;

public class TriggerDevice extends IoDevice {

    public volatile boolean mTriggered;
    public volatile JFrame mFrame;

    public TriggerDevice(int id, Motherboard arch, Path file) {
        super(id, arch, 1);
    }

    protected void deviceClocked() {
        if (mTriggered) {
            mArchitecture.signal(mId, 0);
        }
    }

    protected boolean handleCommand() {
        int command = (int) ((mInput >> 32) & 0xFFFFFFFF);
        switch (command) {
            case ON:
                mFrame = new TriggerFrame();
                mFrame.setVisible(true);
                mOutput = STATUS_OK;
                break;
            case OFF:
                mFrame.dispatchEvent(new WindowEvent(mFrame, WindowEvent.WINDOW_CLOSING));
                mOutput = STATUS_OK;
                break;
            case READ:
                mOutput = (mTriggered) ? 1 : 0;
                mTriggered = false;
                break;
            case WRITE:
                mOutput = STATUS_ERROR;
                break;
        }
        return true;
    }

    public class TriggerFrame extends JFrame {
        private static final long serialVersionUID = 1L;

        public TriggerFrame() {
            super("Trigger");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            final JButton button = new JButton("Trigger");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    mTriggered = true;
                }
            });
            getContentPane().add(button);
            setResizable(false);
            pack();
            setLocationRelativeTo(null);
        }
    }

}
