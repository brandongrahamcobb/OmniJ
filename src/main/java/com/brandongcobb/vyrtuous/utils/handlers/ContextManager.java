package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.List;
import java.util.ArrayList;

import com.brandongcobb.vyrtuous.utils.inc.*;


public class ContextManager {

    private final List<ContextEntry> entries = new ArrayList<>();
    private final int maxEntries;
    
    public ContextManager(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public synchronized void addEntry(ContextEntry entry) {
        entries.add(entry);
        if (entries.size() > maxEntries) {
            // Summarize or remove oldest entries
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
    
    private void summarizeOldEntries() {
        int removeCount = entries.size() / 2;
        for (int i = 0; i < removeCount; i++) entries.remove(0);
    }
    
    public synchronized void clear() {
        entries.clear();
    }
    
    // You can add methods to save/load snapshots from file or DB
}
