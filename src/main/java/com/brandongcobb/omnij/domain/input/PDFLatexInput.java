//
//  is.swift
//  
//
//  Created by Brandon Cobb on 7/5/25.
//


/*  CreateFileInput.java The primary purpose of this class is to
 *  provide input information about the CreateFile.java tool call.
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
package com.brandongcobb.omnij.domain.input;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PDFLatexInput implements ToolInput {

    private final String path;
    private final String outputDirectory;
    private JsonNode originalJson;

    public PDFLatexInput(@JsonProperty("path") String path, @JsonProperty("outputDirectory") String outputDirectory) {
        this.path = path;
        this.outputDirectory = outputDirectory;
    }

    public String getPath() {
        return path;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void setOriginalJson(JsonNode node) {
        this.originalJson = node;
    }

    @Override
    public JsonNode getOriginalJson() {
        return originalJson;
    }
}

