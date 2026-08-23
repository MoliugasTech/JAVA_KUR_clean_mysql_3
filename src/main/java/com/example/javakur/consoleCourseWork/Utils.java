package com.example.javakur.consoleCourseWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public final class Utils {
    private Utils() {
    }

    public static String readLine(BufferedReader input, PrintStream output, String prompt) {
        output.print(prompt);
        try {
            String value = input.readLine();
            return value == null ? null : value.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read console input", e);
        }
    }

    public static int readChoice(BufferedReader input, PrintStream output, String prompt) {
        String value = readLine(input, output, prompt);
        if (value == null) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }
}
