package com.brandongcobb.vyrtuous.domain;

import java.util.List;

public class ShellInput {
    public static class Command {
        public String cmd;
        public String label;
        public String workingDirectory;
        public boolean captureOutput = true;
        public int timeoutSeconds = 30;
        public boolean continueOnFailure = false;

        public List<String> asTokenList() {
            return List.of("/bin/zsh", "-c", cmd);
        }
    }

    public List<Command> commands;
    public String postProcessTool;
    public String explanation;
}
