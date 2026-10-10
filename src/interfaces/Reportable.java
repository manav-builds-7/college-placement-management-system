package interfaces;

/**
 * Any object that can describe itself as a printable report section.
 * Used by ReportService so that one method can print many different
 * kinds of report content.
 */
public interface Reportable {

    /** Returns a multi-line text block that can be printed or saved to a file. */
    String generateReport();
}