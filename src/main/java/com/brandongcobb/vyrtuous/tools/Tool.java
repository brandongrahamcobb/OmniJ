/*  Tool.java The primary purpose of this interface is to create a template for future tools
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
package com.brandongcobb.vyrtuous.tools;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;

public interface Tool<I, O> {
    String getName();
    String getDescription();
    JsonNode getJsonSchema();
    CompletableFuture<O> run(I input) throws Exception;
}

