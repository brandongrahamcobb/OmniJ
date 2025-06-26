/*  PatchOperation.java The primary purpose of this class is to
 *  provide operation information about the Patch.java tool call.
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
package com.brandongcobb.vyrtuous.domain;

public class PatchOperation {
    
    private String code;
    private String match;
    private String replacement;
    private String type;
    
    /*
     *  Getters
     */
    public String getCode() {
        return code;
    }
    
    public String getMatch() {
        return match;
    }

    public String getReplacement() {
        return replacement;
    }
    
    public String getType() {
        return type;
    }

    /*
     *  Setters
     */
    public void setType(String type) {
        this.type = type;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public void setMatch(String match) {
        this.match = match;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}

