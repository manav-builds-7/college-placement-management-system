package ui;

/**
 * Shared presentation for the console menus.
 */
public abstract class ConsoleMenu {

    protected final int width;

    protected ConsoleMenu(int width) {
        this.width = width;
    }

    protected final void printOptions(String title, String... options) {
        System.out.println();
        printTitle(title);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        System.out.println("=".repeat(width));
    }

    protected final void printTitle(String title) {
        System.out.println("=".repeat(width));
        int padding = Math.max(0, (width - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println("=".repeat(width));
    }
}
