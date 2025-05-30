/*  ContextManager.java The purpose of this class is pseudo-MCP
 *  before deployment on a real MCP (model context protocol).
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
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.utils.inc.*;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.Encodings;
import java.util.List;
import java.util.ArrayList;

public class ContextManager {

    private final List<ContextEntry> entries = new ArrayList<>();
    private final int maxEntries;
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    public ContextManager(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public synchronized void addEntry(ContextEntry entry) {
        entries.add(entry);
        if (entries.size() > maxEntries) {  // TODO: Measure by token size not entry size.
            summarizeOldEntries();
        }
    }

    public synchronized String buildPromptContext() {
        StringBuilder sb = new StringBuilder();
        for (ContextEntry entry : entries) {
            sb.append(entry.formatForPrompt()).append("\n");
        }
        return sb.toString();
    }
    
    public synchronized void clear() {
        entries.clear();
    }

    public synchronized long getContextTokenCount() {
        String prompt = buildPromptContext();
        return getTokenCount(prompt);
    }

    private long getTokenCount(String prompt) {
        try {
            Encoding encoding = registry.getEncoding("cl100k_base")
                .orElseThrow(() -> new IllegalStateException("Encoding cl100k_base not available"));
            return encoding.encode(prompt).size();
        } catch (Exception e) {
            return 0L;
        }
    }

    private String summarizeEntries(List<ContextEntry> entriesToSummarize) {
        StringBuilder sb = new StringBuilder();
        for (ContextEntry e : entriesToSummarize) {
            String c = e.formatForPrompt();
            String snippet = c.length() > 100 ? c.substring(0, 100) + "..." : c;
            sb.append(snippet).append(" ");
        }
        return sb.toString().trim();
    }

    private synchronized void summarizeOldEntries() {
        int removeCount = entries.size() / 2;
        List<ContextEntry> toSummarize = new ArrayList<>(entries.subList(0, removeCount));
        String summary = summarizeEntries(toSummarize);
        for (int i = 0; i < removeCount; i++) {
            entries.remove(0);
        }
        entries.add(0, new ContextEntry(ContextEntry.Type.SYSTEM_NOTE, "[Summary of earlier context]: " + summary));
    }
}
