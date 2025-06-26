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

import com.brandongcobb.vyrtuous.Vyrtuous;
import com.brandongcobb.vyrtuous.objects.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.Encodings;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import com.brandongcobb.vyrtuous.utils.secure.*;

public class ContextManager {

    private final List<ContextEntry> entries = new ArrayList<>();
    private final int maxEntries;
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private int lastBuildIndex = 0;

    public ContextManager(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public List<ContextEntry> getEntries() {
        return entries;
    }
    
    public List<String> listSnapshots() {
        File dir = new File("snapshots");
        if (!dir.exists()) return List.of();

        String[] files = dir.list((d, name) -> name.endsWith(".json"));
        if (files == null) return List.of();

        return Arrays.stream(files)
            .map(name -> name.replaceFirst("\\.json$", ""))
            .toList();
    }

    public void printNewEntries(boolean includeUserMessages,
                                boolean includeAIResponses,
                                boolean includeToolCalls,
                                boolean includeToolOutputs,
                                boolean includeTokens,
                                boolean includeSystemNotes,
                                boolean includeProgressiveSummary,
                                boolean includeShellOutput) {

        List<ContextEntry> newEntries = getNewEntriesSinceLastCall();

        for (ContextEntry entry : newEntries) {
            ContextEntry.Type type = entry.getType();

            boolean shouldPrint =
                (type == ContextEntry.Type.USER_MESSAGE     && includeUserMessages)   ||
                (type == ContextEntry.Type.AI_RESPONSE      && includeAIResponses)    ||
                (type == ContextEntry.Type.TOOL         && includeToolCalls)       ||
                (type == ContextEntry.Type.TOOL_OUTPUT   && includeToolOutputs) ||
                (type == ContextEntry.Type.TOKENS           && includeTokens)         ||
                (type == ContextEntry.Type.SYSTEM_NOTE      && includeSystemNotes)    ||
                (type == ContextEntry.Type.PROGRESSIVE_SUMMARY && includeProgressiveSummary) ||
                (type == ContextEntry.Type.SHELL_OUTPUT     && includeShellOutput);

            if (shouldPrint) {
                String color;
                switch (type) {
                    case USER_MESSAGE:   color = Vyrtuous.BRIGHT_BLUE; break;
                    case AI_RESPONSE:    color = Vyrtuous.TEAL;        break;
                    case TOOL:        color = Vyrtuous.CYAN;        break;
                    case TOOL_OUTPUT: color = Vyrtuous.SKY_BLUE;    break;
                    case TOKENS:         color = Vyrtuous.BRIGHT_CYAN; break;
                    case SYSTEM_NOTE:    color = Vyrtuous.NAVY;        break;
                    case SHELL_OUTPUT:   color = Vyrtuous.DODGER_BLUE; break;
                    default:             color = Vyrtuous.RESET;       break;
                }
                System.out.println(color + entry.formatForPrompt() + Vyrtuous.RESET);
            }
        }
    }

    public synchronized List<ContextEntry> getNewEntriesSinceLastCall() {
        if (lastBuildIndex >= entries.size()) {
            return new ArrayList<>();
        }
        List<ContextEntry> newEntries = new ArrayList<>(entries.subList(lastBuildIndex, entries.size()));
        lastBuildIndex = entries.size();
        return newEntries;
    }

    
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

    
    public synchronized String buildPromptContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("The user originally asked:\n").append(extractOriginalGoal()).append("\n\n");
        for (ContextEntry entry : entries) {
            sb.append(entry.formatForPrompt()).append("\n\n");
        } // <--- Mark the point at which context was built
        return sb.toString();
    }

    public synchronized void clear() {
        entries.clear(); // Commented out
    }
    
    public synchronized void clearModified() {
        List<ContextEntry> preserved = new ArrayList<>();

        for (ContextEntry entry : entries) {
            ContextEntry.Type type = entry.getType();
            if (type == ContextEntry.Type.USER_MESSAGE || type == ContextEntry.Type.PROGRESSIVE_SUMMARY || type == ContextEntry.Type.TOOL) {
                preserved.add(entry);
            }
            // All other types are excluded
        }

        entries.clear();
        entries.addAll(preserved);
        lastBuildIndex = entries.size(); // Keep index in sync
    }
    
    public synchronized void clearBetweenSaves() {
        List<ContextEntry> preserved = new ArrayList<>();

        for (ContextEntry entry : entries) {
            ContextEntry.Type type = entry.getType();
            if (type == ContextEntry.Type.USER_MESSAGE || type == ContextEntry.Type.TOOL || type == ContextEntry.Type.TOOL_OUTPUT) {
                preserved.add(entry);
            }
            // All other types are excluded
        }

        entries.clear();
        entries.addAll(preserved);
        lastBuildIndex = entries.size(); // Keep index in sync
    }

    public synchronized long getContextTokenCount() {
        String prompt = buildPromptContext();
        return getTokenCount(prompt);
    }

    public long getTokenCount(String prompt) {
        try {
            Encoding encoding = registry.getEncoding("cl100k_base")
                .orElseThrow(() -> new IllegalStateException("Encoding cl100k_base not available"));
            return encoding.encode(prompt).size() * 2;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    public void printEntries(boolean includeUserMessages,
                             boolean includeAIResponses,
                             boolean includeToolCalls,
                             boolean includeToolOutputs,
                             boolean includeTokens,
                             boolean includeSystemNotes,
                             boolean includeProgressiveSummary,
                             boolean includeShellOutput) {

        for (ContextEntry entry : entries) {
            ContextEntry.Type type = entry.getType();

            boolean shouldPrint =
                (type == ContextEntry.Type.USER_MESSAGE     && includeUserMessages)   ||
                (type == ContextEntry.Type.AI_RESPONSE      && includeAIResponses)    ||
                (type == ContextEntry.Type.TOOL            && includeToolCalls)       ||
                (type == ContextEntry.Type.TOOL_OUTPUT      && includeToolOutputs) ||
                (type == ContextEntry.Type.TOKENS           && includeTokens)         ||
                (type == ContextEntry.Type.SYSTEM_NOTE      && includeSystemNotes)    ||
                (type == ContextEntry.Type.PROGRESSIVE_SUMMARY  && includeProgressiveSummary) ||
                (type == ContextEntry.Type.SHELL_OUTPUT     && includeShellOutput);

            if (shouldPrint) {
                String color;
                switch (type) {
                    case USER_MESSAGE:   color = Vyrtuous.BRIGHT_BLUE; break;
                    case AI_RESPONSE:    color = Vyrtuous.TEAL;        break;
                    case TOOL:        color = Vyrtuous.CYAN;        break;
                    case TOOL_OUTPUT: color = Vyrtuous.SKY_BLUE;    break;
                    case TOKENS:         color = Vyrtuous.BRIGHT_CYAN; break;
                    case SYSTEM_NOTE:    color = Vyrtuous.NAVY;        break;
                    case PROGRESSIVE_SUMMARY: color = Vyrtuous.NAVY;    break;
                    case SHELL_OUTPUT:   color = Vyrtuous.DODGER_BLUE; break;
                    default:             color = Vyrtuous.RESET;       break;
                }
                System.out.println(color + entry.formatForPrompt() + Vyrtuous.RESET);
            }
        }
    }


   public int countEntries() {
       return entries.size();
   }
    
    public String summarizeEntries(List<ContextEntry> entriesToSummarize) {
        StringBuilder sb = new StringBuilder();
        for (ContextEntry e : entriesToSummarize) {
            String c = e.formatForPrompt();
            String snippet = c.length() > 100 ? c.substring(0, 100) + "..." : c;
            sb.append(snippet).append(" ");
        }
        return sb.toString().trim();
    }

    public synchronized void summarizeOldEntries() {
        int removeCount = entries.size() / 2;
        List<ContextEntry> toSummarize = new ArrayList<>(entries.subList(0, removeCount));
        String summary = summarizeEntries(toSummarize);
        for (int i = 0; i < removeCount; i++) {
            entries.remove(0);
        }
        entries.add(0, new ContextEntry(ContextEntry.Type.SYSTEM_NOTE, "[Summary of earlier context]: " + summary)); // Commented out
    }
    
    public synchronized void saveSnapshot(String name, String description) {
        String passwordEnv = System.getenv("PASSWORD");
        char[] password = passwordEnv.toCharArray();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        File dir = new File("snapshots");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, name + ".enc");

        Snapshot snapshot = new Snapshot();
        snapshot.description = description;
        snapshot.entries = new ArrayList<>(this.entries);

        try {
            byte[] jsonBytes = mapper.writeValueAsBytes(snapshot);
            Encryption.encryptToFile(jsonBytes, file, password);
            Arrays.fill(password, '\0');
        } catch (Exception e) {
            throw new RuntimeException("Failed to save encrypted snapshot: " + e.getMessage(), e);
        }
    }

    public synchronized void loadSnapshot(String name) {
        String passwordEnv = System.getenv("PASSWORD");
        char[] password = passwordEnv.toCharArray();
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("snapshots", name + ".enc");

        if (!file.exists()) {
            throw new IllegalArgumentException("Snapshot '" + name + "' not found.");
        }

        try {
            byte[] jsonBytes = Encryption.decryptFromFile(file, password);
            Arrays.fill(password, '\0');
            Snapshot snapshot = mapper.readValue(jsonBytes, Snapshot.class);
            this.entries.clear();
            this.entries.addAll(snapshot.entries);
            this.lastBuildIndex = entries.size();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load encrypted snapshot: " + e.getMessage(), e);
        }
    }

    private static class Snapshot {
        public String description;
        public List<ContextEntry> entries;
    }
}
