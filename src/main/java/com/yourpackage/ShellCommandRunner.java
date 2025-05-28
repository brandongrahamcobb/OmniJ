package com.yourpackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

public class ShellCommandRunner {
    public static class CommandResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;
        public CommandResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
    public static CommandResult runShellCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
        pb.redirectErrorStream(false);
        Process process = pb.start();
        String stdout = readInputStream(process.getInputStream());
        String stderr = readInputStream(process.getErrorStream());
        int exitCode = process.waitFor();
        return new CommandResult(exitCode, stdout, stderr);
    }
    private static String readInputStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
