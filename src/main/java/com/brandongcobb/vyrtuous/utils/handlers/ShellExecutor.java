//package com.brandongcobb.vyrtuous.utils.handlers;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.*;
//
//public class ShellExecutor {
//    public static class Result {
//        public final int exitCode;
//        public final String stdout;
//        public final String stderr;
//
//        public Result(int exitCode, String stdout, String stderr) {
//            this.exitCode = exitCode;
//            this.stdout   = stdout;
//            this.stderr   = stderr;
//        }
//    }
//
//    /** Run a command in the user's login shell (SHELL or /bin/bash). */
//    public static Result runCommand(String command) throws IOException, InterruptedException {
//        String shell = System.getenv("SHELL");
//        if (shell == null || shell.isEmpty()) shell = "/bin/bash";
//
//        List<String> cmd = List.of(shell, "-lc", command);
//        ProcessBuilder pb = new ProcessBuilder(cmd);
//        pb.environment().putAll(System.getenv());
//        pb.redirectErrorStream(false);
//        Process proc = pb.start();
//
//        ExecutorService pool = Executors.newFixedThreadPool(2);
//        Future<String> outF = pool.submit(() -> readStream(proc.getInputStream()));
//        Future<String> errF = pool.submit(() -> readStream(proc.getErrorStream()));
//
//        proc.waitFor();
//        String out = "", err = "";
//        try { out = outF.get(); } catch (Exception ignored) {}
//        try { err = errF.get(); } catch (Exception ignored) {}
//        pool.shutdownNow();
//
//        return new Result(proc.exitValue(), out, err);
//    }
//
//    private static String readStream(InputStream is) throws IOException {
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(is, StandardCharsets.UTF_8))) {
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//            return sb.toString();
//        }
//    }
//}
