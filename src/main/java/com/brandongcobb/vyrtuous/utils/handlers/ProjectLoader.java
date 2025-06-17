/*  ProjectLoader.java The purpose of this class is to load the
 *  files crudely into a superstring.
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
 *  aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.utils.handlers;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;

public class ProjectLoader {
    
    public static String loadProjectSource() {
        Path sourceRoot = Paths.get("/app/src");
        try {
            return Files.walk(sourceRoot)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> {
                    try {
                        return Files.readString(p);
                    } catch (IOException e) {
                        return "";
                    }
                })
                .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
