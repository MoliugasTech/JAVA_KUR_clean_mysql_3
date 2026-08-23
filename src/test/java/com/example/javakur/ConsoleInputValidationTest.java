package com.example.javakur;

import com.example.javakur.consoleCourseWork.Utils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInputValidationTest {

    @Test
    void acceptsHourWithoutMinutes() {
        assertEquals(LocalTime.of(9, 0), readTime("09\n").value());
    }

    @Test
    void repeatsOnlyInvalidTimeField() {
        ReadResult<LocalTime> result = readTime("wrong\n21:30\n");

        assertEquals(LocalTime.of(21, 30), result.value());
        assertTrue(result.output().contains("Neteisingas laikas"));
    }

    @Test
    void acceptsCommaPriceAndRepeatsInvalidValue() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BigDecimal price = Utils.readPositiveDecimal(
                new BufferedReader(new StringReader("-2\n9,50\n")),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), "Kaina: ");

        assertEquals(new BigDecimal("9.50"), price);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Įveskite teigiamą skaičių"));
    }

    @Test
    void repeatsBlankRequiredTextAndInvalidEmail() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        String name = Utils.readRequired(
                new BufferedReader(new StringReader("   \nAustėja\n")), output, "Vardas: ");
        String email = Utils.readEmail(
                new BufferedReader(new StringReader("blogas\nausteja@example.com\n")), output, "Email: ");

        assertEquals("Austėja", name);
        assertEquals("austeja@example.com", email);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("privalomas"));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("tinkamą el. pašto"));
    }

    @Test
    void repeatsOutOfRangeChoiceAndAmbiguousConfirmation() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        int choice = Utils.readChoiceInRange(
                new BufferedReader(new StringReader("99\n2\n")), output, "Pasirinkimas: ", 0, 3);
        boolean confirmed = Utils.readConfirmation(
                new BufferedReader(new StringReader("gal\ntaip\n")), output, "Patvirtinti: ");

        assertEquals(2, choice);
        assertTrue(confirmed);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("nuo 0 iki 3"));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Atsakykite"));
    }

    private ReadResult<LocalTime> readTime(String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        LocalTime value = Utils.readTime(
                new BufferedReader(new StringReader(input)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), "Laikas: ");
        return new ReadResult<>(value, bytes.toString(StandardCharsets.UTF_8));
    }

    private record ReadResult<T>(T value, String output) {
    }
}
