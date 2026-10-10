package file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Saves report text to the reports/ folder as UTF-8 text files.
 * Only the file names defined here are used, so callers cannot write anywhere else.
 */
public class ReportFileHandler {

    public static final String PLACEMENT_REPORT_FILE = "placement_report.txt";
    public static final String STUDENT_REPORT_FILE = "student_report.txt";
    public static final String COMPANY_REPORT_FILE = "company_report.txt";
    public static final String APPLICATION_REPORT_FILE = "application_report.txt";

    private static final Path REPORT_DIRECTORY = Paths.get("reports");

    /**
     * Writes the text to reports/<fileName>, creating the folder if it does not exist.
     * An existing file with the same name is replaced.
     *
     * @return the path of the saved file
     */
    public Path saveReport(String fileName, String content) throws IOException {
        Files.createDirectories(REPORT_DIRECTORY);
        Path target = REPORT_DIRECTORY.resolve(fileName);
        Files.writeString(target, content, StandardCharsets.UTF_8);
        return target;
    }
}