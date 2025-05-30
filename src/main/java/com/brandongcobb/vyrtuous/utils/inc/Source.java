/*  FileObject.java
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

import com.brandongcobb.vyrtuous.utils.inc.*;

public enum Source {

    AI_MANAGER(Helpers.FILE_AI_MANAGER),
    DISCORD_BOT(Helpers.FILE_DISCORD_BOT),
    EVENT_LISTENERS(Helpers.FILE_EVENT_LISTENERS),
    HELPERS(Helpers.FILE_HELPERS),
    HYBIRD_COMMANDS(Helpers.FILE_HYBRID_COMMANDS),
    MESSAGE_MANAGER(Helpers.FILE_MESSAGE_MANAGER),
    MODEL_INFO(Helpers.FILE_MODEL_INFO),
    MODEL_REGISTRY(Helpers.FILE_MODEL_REGISTRY),
    MODERATION_MANAGER(Helpers.FILE_MODERATION_MANAGER),
    PREDICATOR(Helpers.FILE_PREDICATOR),
    RESPONSE_OBJECT(Helpers.FILE_RESPONSE_OBJECT),
    VYRTUOUS(Helpers.FILE_VYRTUOUS);

    public final String fileContent;

    Source(String fileContent) {
        this.fileContent = fileContent;
    }
}
