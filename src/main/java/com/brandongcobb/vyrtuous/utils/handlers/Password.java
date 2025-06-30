/*  Password.java The purpose of this class is to provide the password
 *  for encrypting snapshots.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.Console;

public class Password {

    public static char[] promptPassword(String promptMessage) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(promptMessage);
        } else {
            System.err.println("WARNING: Console not available. Password will be visible.");
            System.out.print(promptMessage);
            return System.console().readLine().toCharArray();
        }
    }
}
