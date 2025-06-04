/* ContextManager.java The purpose of this class is pseudo-MCP
 * before deployment on a real MCP (model context protocol).
 *
 * Copyright (C) 2025  github.com/brandongrahamcobb
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
 * aInteger with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry(); // Commented out as token counting is disabled

    public ContextManager(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Adds a new context entry.
     * The core logic for adding and managing entries is commented out as per user request
     * to disable context accumulation for the current REPL flow.
     * @param entry The ContextEntry to add.
     */
    public synchronized void addEntry(ContextEntry entry) {
        entries.add(entry);
        if (entries.size() > maxEntries) { // TODO: Measure by token size not entry size.
            summarizeOldEntries();
        }
    }

    public String extractOriginalGoal() {
        return entries.stream()
            .filter(e -> e.getType() == ContextEntry.Type.USER_MESSAGE)
            .map(ContextEntry::getContent)
            .findFirst()
            .orElse("No user message found.");
    }
    /**
     * Builds the prompt context from accumulated entries.
     * Returns an empty string as context building is currently disabled.
     * @return An empty string, as context building is disabled.
     */
    public synchronized String buildPromptContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("The user originally asked:\n").append(extractOriginalGoal()).append("\n\n");
        for (ContextEntry entry : entries) {
            sb.append(entry.formatForPrompt()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Clears all context entries.
     * The core logic is commented out as context management is disabled.
     */
    public synchronized void clear() {
        entries.clear(); // Commented out
    }

    /**
     * Calculates the token count of the current prompt context.
     * Returns 0L as token counting is currently disabled.
     * @return 0L, as token counting is disabled.
     */
    public synchronized long getContextTokenCount() {
        String prompt = buildPromptContext();
        return getTokenCount(prompt);
    }

    /**
     * Calculates the token count for a given prompt string.
     * Returns 0L as token counting is currently disabled.
     * @param prompt The prompt string to count tokens for.
     * @return 0L, as token counting is disabled.
     */
    private long getTokenCount(String prompt) {
        try {
            Encoding encoding = registry.getEncoding("cl100k_base")
                .orElseThrow(() -> new IllegalStateException("Encoding cl100k_base not available"));
            return encoding.encode(prompt).size();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Summarizes old context entries.
     * The core logic is commented out as context management is disabled.
     * @param entriesToSummarize The list of entries to summarize.
     * @return An empty string, as summarization is disabled.
     */
    private String summarizeEntries(List<ContextEntry> entriesToSummarize) {
        StringBuilder sb = new StringBuilder();
        for (ContextEntry e : entriesToSummarize) {
            String c = e.formatForPrompt();
            String snippet = c.length() > 100 ? c.substring(0, 100) + "..." : c;
            sb.append(snippet).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Summarizes and removes old entries when the context exceeds maxEntries.
     * The core logic is commented out as context management is disabled.
     */
    private synchronized void summarizeOldEntries() {
        int removeCount = entries.size() / 2;
        List<ContextEntry> toSummarize = new ArrayList<>(entries.subList(0, removeCount));
        String summary = summarizeEntries(toSummarize);
        for (int i = 0; i < removeCount; i++) {
            entries.remove(0);
        }
        entries.add(0, new ContextEntry(ContextEntry.Type.SYSTEM_NOTE, "[Summary of earlier context]: " + summary)); // Commented out
    }
}
