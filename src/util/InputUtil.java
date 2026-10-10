package util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Reads typed input safely. Every method keeps asking until the value is valid,
 * so invalid typing never crashes the program.
 *
 * The Scanner is passed in from outside rather than created here. That avoids
 * static global state and lets tests supply a fake input string.
 */
public class InputUtil {

    private final Scanner scanner;

    public InputUtil(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Reads one line of text. May be empty. */
    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /** Reads text and repeats the prompt until the line is not blank. */
    public String readRequiredString(String prompt) {
        while (true) {
            String value = readString(prompt).trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field is required. Please enter a value.");
        }
    }

    /**
     * Reads text where pressing Enter keeps the current value.
     * Used when updating an existing record.
     */
    public String readOptionalString(String prompt, String currentValue) {
        String value = readString(prompt + " [" + currentValue + "]: ").trim();
        return value.isEmpty() ? currentValue : value;
    }

    /** Reads a whole number, repeating until it is valid. */
    public int readInt(String prompt) {
        while (true) {
            String line = readString(prompt).trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    /** Reads a decimal number, repeating until it is valid. */
    public double readDouble(String prompt) {
        while (true) {
            String line = readString(prompt).trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number, for example 7.5.");
            }
        }
    }

    /** Reads a date in yyyy-mm-dd format, repeating until it is valid. */
    public LocalDate readDate(String prompt) {
        while (true) {
            String line = readString(prompt).trim();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a date as yyyy-mm-dd, for example 2026-12-31.");
            }
        }
    }

    /** Reads a time in HH:mm format (24-hour), repeating until it is valid. */
    public LocalTime readTime(String prompt) {
        while (true) {
            String line = readString(prompt).trim();
            try {
                return LocalTime.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a time as HH:mm, for example 14:30.");
            }
        }
    }

    /**
     * Reads a menu choice between min and max (inclusive).
     * Used by every menu in the UI layer.
     */
    public int readMenuChoice(String prompt, int min, int max) {
        while (true) {
            int choice = readInt(prompt);
            if (choice >= min && choice <= max) {
                return choice;
            }
            System.out.println("Please choose a number from " + min + " to " + max + ".");
        }
    }

    /** Reads a yes/no answer. Accepts y, yes, n, no in any case. */
    public boolean readYesNo(String prompt) {
        while (true) {
            String answer = readString(prompt).trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }
}