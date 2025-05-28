package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.List;
import java.util.ArrayList;

import com.brandongcobb.vyrtuous.utils.inc.*;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.Encodings;
public class ContextManager {

    private final List<ContextEntry> entries = new ArrayList<>();
    private final int maxEntries;          // max number of entries before summarizing
    private EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    public ContextManager(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public synchronized void addEntry(ContextEntry entry) {
        entries.add(entry);

        // Optional: summarize if entries too large
        if (entries.size() > maxEntries) {
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
            // Log or handle error if needed
            return 0L;
        }
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

    private String summarizeEntries(List<ContextEntry> entriesToSummarize) {
        StringBuilder sb = new StringBuilder();
        for (ContextEntry e : entriesToSummarize) {
            String c = e.formatForPrompt();
            String snippet = c.length() > 100 ? c.substring(0, 100) + "..." : c;
            sb.append(snippet).append(" ");
        }
        return sb.toString().trim();
    }

    public synchronized void clear() {
        entries.clear();
    }
}
