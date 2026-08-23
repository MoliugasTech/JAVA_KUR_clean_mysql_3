package com.example.javakur.consoleCourseWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    public static int readChoiceInRange(BufferedReader input, PrintStream output,
                                        String prompt, int minimum, int maximum) {
        while (true) {
            int choice = readChoice(input, output, prompt);
            if (choice >= minimum && choice <= maximum) return choice;
            output.println("Pasirinkite numerį nuo " + minimum + " iki " + maximum + ".");
        }
    }

    public static String readRequired(BufferedReader input, PrintStream output, String prompt) {
        while (true) {
            String value = readLine(input, output, prompt);
            if (value != null && !value.isBlank()) return value;
            output.println("Šis laukas yra privalomas.");
        }
    }

    public static String readMinLength(BufferedReader input, PrintStream output,
                                       String prompt, int minimumLength) {
        while (true) {
            String value = readRequired(input, output, prompt);
            if (value.length() >= minimumLength) return value;
            output.println("Įveskite bent " + minimumLength + " simbolius.");
        }
    }

    public static String readEmail(BufferedReader input, PrintStream output, String prompt) {
        while (true) {
            String value = readRequired(input, output, prompt);
            if (value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return value;
            output.println("Įveskite tinkamą el. pašto adresą, pvz., vardas@example.com.");
        }
    }

    public static int readPositiveInt(BufferedReader input, PrintStream output, String prompt) {
        while (true) {
            int value = readChoice(input, output, prompt);
            if (value > 0) return value;
            output.println("Įveskite teigiamą sveiką skaičių.");
        }
    }

    public static boolean readConfirmation(BufferedReader input, PrintStream output, String prompt) {
        while (true) {
            String value = readRequired(input, output, prompt).toLowerCase();
            if (value.equals("t") || value.equals("taip") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("ne") || value.equals("no")) return false;
            output.println("Atsakykite „taip“ arba „ne“.");
        }
    }

    public static LocalTime readTime(BufferedReader input, PrintStream output, String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        while (true) {
            String value = readLine(input, output, prompt);
            if (value == null) throw new IllegalStateException("Input was closed");
            if (value.matches("\\d{1,2}")) value += ":00";
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                output.println("Neteisingas laikas. Įveskite, pvz., 9, 09:00 arba 21:30.");
            }
        }
    }

    public static BigDecimal readPositiveDecimal(BufferedReader input, PrintStream output,
                                                 String prompt) {
        while (true) {
            String value = readLine(input, output, prompt);
            if (value == null) throw new IllegalStateException("Input was closed");
            try {
                BigDecimal number = new BigDecimal(value.replace(',', '.'));
                if (number.compareTo(BigDecimal.ZERO) > 0) return number;
            } catch (NumberFormatException ignored) {
                // A clear validation message is printed below.
            }
            output.println("Įveskite teigiamą skaičių, pvz., 9.50.");
        }
    }

    public static BigDecimal readOptionalPositiveDecimal(BufferedReader input, PrintStream output,
                                                         String prompt) {
        while (true) {
            String value = readLine(input, output, prompt);
            if (value == null || value.isBlank()) return null;
            try {
                BigDecimal number = new BigDecimal(value.replace(',', '.'));
                if (number.compareTo(BigDecimal.ZERO) > 0) return number;
            } catch (NumberFormatException ignored) {
                // A clear validation message is printed below.
            }
            output.println("Įveskite teigiamą skaičių arba palikite lauką tuščią.");
        }
    }

    public static Boolean readOptionalBoolean(BufferedReader input, PrintStream output, String prompt) {
        while (true) {
            String value = readLine(input, output, prompt);
            if (value == null || value.isBlank()) return null;
            value = value.toLowerCase();
            if (value.equals("t") || value.equals("taip") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("ne") || value.equals("no")) return false;
            output.println("Įveskite „taip“, „ne“ arba palikite lauką tuščią.");
        }
    }
}
