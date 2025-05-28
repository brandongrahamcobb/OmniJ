import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ShellExecutor {
    public static class Result {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        public Result(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

    /**
     * Run a shell command using the user's default login shell. 0 timeout means wait indefinitely.
     */
    public static Result runCommand(String shellCommand) throws IOException, InterruptedException {
        return runCommand(shellCommand, 0);
    }

    /**
     * Run a shell command with an optional timeout in seconds. 0 means no timeout.
     */
    public static Result runCommand(String shellCommand, long timeoutSeconds) throws IOException, InterruptedException {
        String userShell = System.getenv("SHELL");
        if (userShell == null || userShell.isEmpty()) {
            userShell = "/bin/bash";
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(userShell);
        cmd.add("-lc");
        cmd.add(shellCommand);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process process = pb.start();

        ExecutorService exec = Executors.newFixedThreadPool(2);
        Future<String> outF = exec.submit(() -> readStream(process.getInputStream()));
        Future<String> errF = exec.submit(() -> readStream(process.getErrorStream()));

        if (timeoutSeconds > 0) {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } else {
            process.waitFor();
        }

        String out = "";
        String err = "";
        try { out = outF.get(); } catch (Exception ignored) {}
        try { err = errF.get(); } catch (Exception ignored) {}

        exec.shutdownNow();
        return new Result(process.exitValue(), out, err);
    }

    /**
     * Read all data from InputStream into a String.
     */
    private static String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
