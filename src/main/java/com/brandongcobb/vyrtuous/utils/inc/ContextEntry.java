package com.brandongcobb.vyrtuous.utils.inc;

public class ContextEntry {
    public enum Type { USER_MESSAGE, AI_RESPONSE, COMMAND, COMMAND_OUTPUT, SYSTEM_NOTE }
    
    private final Type type;
    private final String content;
    
    public ContextEntry(Type type, String content) {
        this.type = type;
        this.content = content;
    }
    
    public String formatForPrompt() {
        switch(type) {
            case USER_MESSAGE: return "[User]: " + content;
            case AI_RESPONSE: return "[AI]: " + content;
            case COMMAND: return "[Command]: " + content;
            case COMMAND_OUTPUT: return "[Output]: " + content;
            case SYSTEM_NOTE: return "[System]: " + content;
            default: return content;
        }
    }
}
