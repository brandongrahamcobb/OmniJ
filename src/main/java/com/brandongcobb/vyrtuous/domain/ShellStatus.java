///*  ShellStatus.java The primary purpose of this interface is to serve
// *  as the template for status classes for tools.
// *
// *  Copyright (C) 2025  github.com/brandongrahamcobb
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//package com.brandongcobb.vyrtuous.domain;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ShellStatus {
//
//    public static class CommandResult {
//        public String label;
//        public int exitCode;
//        public String stdout;
//        public String stderr;
//        public boolean success;
//
//        // Getters
//        public String getLabel() {
//            return label;
//        }
//
//        public int getExitCode() {
//            return exitCode;
//        }
//
//        public String getStdout() {
//            return stdout;
//        }
//
//        public String getStderr() {
//            return stderr;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        // Setters (optional)
//        public void setLabel(String label) {
//            this.label = label;
//        }
//
//        public void setExitCode(int exitCode) {
//            this.exitCode = exitCode;
//        }
//
//        public void setStdout(String stdout) {
//            this.stdout = stdout;
//        }
//
//        public void setStderr(String stderr) {
//            this.stderr = stderr;
//        }
//
//        public void setSuccess(boolean success) {
//            this.success = success;
//        }
//        private static String getCmdSummary() {
//            return "[unnamed command]";
//        }
//    }
//
//    public List<CommandResult> results = new ArrayList<>();
//    private String summary;
//
//    // Getters
//    public List<CommandResult> getResults() {
//        return results;
//    }
//
//    public String getSummary() {
//        return summary;
//    }
//
//    // Setters (optional)
//    public void setResults(List<CommandResult> results) {
//        this.results = results;
//    }
//
//    public void setSummary(String summary) {
//        this.summary = summary;
//    }
//
//    // Message generation
//    public String getMessage() {
//        if (results.isEmpty()) {
//            return "No commands were executed.";
//        }
//
//        return results.stream()
//            .map(r -> {
//                String prefix = r.isSuccess() ? "✅" : "❌";
//                return String.format("%s %s (exit: %d)\nstdout:\n%s\nstderr:\n%s\n",
//                    prefix,
//                    r.getLabel() != null ? r.getLabel() : r.getCmdSummary(),
//                    r.getExitCode(),
//                    trimOrDefault(r.getStdout(), "(no stdout)"),
//                    trimOrDefault(r.getStderr(), "(no stderr)")
//                );
//            })
//            .collect(Collectors.joining("\n"));
//    }
//
//    // Utility for short-circuit summaries if label is null
//    private static String trimOrDefault(String value, String def) {
//        return (value != null && !value.trim().isEmpty()) ? value.trim() : def;
//    }
//
//    // Optional fallback if label is null (not used in your current code)
//
//}
