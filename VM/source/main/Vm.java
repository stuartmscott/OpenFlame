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
package main;

import architecture.Card;
import architecture.Motherboard;
import architecture.iodevice.CharacterPrinter;
import architecture.iodevice.IoDevice;

public class Vm {

    public static void main(String[] args) {
        String program = args[0];// Expected to hold the path to file which will be copied into ram and executed.
        Card[] cards = new Card[Motherboard.NUM_CARDS];
        IoDevice[] ioDevices = new IoDevice[Motherboard.NUM_DEVICES];
        Motherboard motherboard = new Motherboard(cards, ioDevices, program);
        ioDevices[0] = new CharacterPrinter(8, motherboard);
        // String storage = null;
        // ioDevices[1] = new StorageDrive(9, architecture, storage);
        for (int id = 0; id < Motherboard.NUM_CARDS; id++) {
            cards[id] = new Card(id, motherboard);
        }
        motherboard.start();
        motherboard.signal(0, 0);// Start the first context of the first card
    }
}
