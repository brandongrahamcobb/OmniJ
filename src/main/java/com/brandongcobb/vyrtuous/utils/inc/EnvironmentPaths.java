/*  EnvironmentPaths.java The purpose of this program is to hold path variables for
 *  the main program.
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
package com.brandongcobb.vyrtuous.utils.inc;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum EnvironmentPaths {
    AI_MANAGER(Helpers.PATH_AI_MANAGER),
    COG(Helpers.PATH_COG),
    DISCORD_BOT(Helpers.PATH_DISCORD_BOT),
    EVENT_LISTENERS(Helpers.PATH_EVENT_LISTENERS),
    HELPERS(Helpers.PATH_HELPERS),
    HYBRID_COMMANDS(Helpers.PATH_HYBRID_COMMANDS),
    MESSAGE_MANAGER(Helpers.PATH_MESSAGE_MANAGER),
    MODEL_INFO(Helpers.PATH_MODEL_INFO),
    MODEL_REGISTRY(Helpers.PATH_MODEL_REGISTRY),
    MODERATION_MANAGER(Helpers.PATH_MODERATION_MANAGER),
    PREDICATOR(Helpers.PATH_PREDICATOR),
    RESPONSE_OBJECT(Helpers.PATH_RESPONSE_OBJECT),
    VYRTUOUS(Helpers.PATH_VYRTUOUS);

    private final Path path;

    EnvironmentPaths(Path relativePath) {
        this.path = Helpers.DIR_BASE.resolve(relativePath);
    }

    Path get() {
        return path;
    }
}
