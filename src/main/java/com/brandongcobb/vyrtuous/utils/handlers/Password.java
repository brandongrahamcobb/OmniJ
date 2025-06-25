//
//  PasswordPrompt.swift
//  
//
//  Created by Brandon Cobb on 6/25/25.
//


import java.io.Console;

public class Password {

    public static char[] promptPassword(String promptMessage) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(promptMessage); // Hidden input
        } else {
            // Fallback: Use standard input (NOT hidden), e.g., in IDEs or some terminals
            System.err.println("WARNING: Console not available. Password will be visible.");
            System.out.print(promptMessage);
            return System.console().readLine().toCharArray();  // still fallback
        }
    }
}
