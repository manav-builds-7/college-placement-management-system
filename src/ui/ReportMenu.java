package ui;

import file.ReportFileHandler;
import service.ReportService;
import util.InputUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Lets the officer print any report to the terminal and optionally save it as a TXT file.
 * ReportService builds the text, and ReportFileHandler writes it.
 */
public class ReportMenu extends ConsoleMenu {

    private final InputUtil input;
    private final ReportService reportService;
    private final ReportFileHandler fileHandler;

    public ReportMenu(InputUtil input, ReportService reportService, ReportFileHandler fileHandler) {
        super(64);
        this.input = input;
        this.reportService = reportService;
        this.fileHandler = fileHandler;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("REPORTS", "Student report", "Company report", "Job report",
                    "Application report", "Interview report", "Placement list",
                    "Department-wise placement", "Company-wise placement",
                    "Package statistics", "Complete placement report", "Back");
            int choice = input.readMenuChoice("Choose a report: ", 1, 11);

            if (choice == 11) {
                back = true;
            } else {
                runSafely(() -> showAndSave(choice));
            }
        }
    }

    /** Builds the chosen report, prints it, then asks whether to save it. */
    private void showAndSave(int choice) throws SQLException, IOException {
        String text;
        String fileName;

        switch (choice) {
            case 1: text = reportService.studentReport(); fileName = "student_report.txt"; break;
            case 2: text = reportService.companyReport(); fileName = "company_report.txt"; break;
            case 3: text = reportService.jobReport(); fileName = "job_report.txt"; break;
            case 4: text = reportService.applicationReport(); fileName = ReportFileHandler.APPLICATION_REPORT_FILE; break;
            case 5: text = reportService.interviewReport(); fileName = "interview_report.txt"; break;
            case 6: text = reportService.placementReport(); fileName = "placement_list.txt"; break;
            case 7: text = reportService.departmentReport(); fileName = "department_report.txt"; break;
            case 8: text = reportService.companyWiseReport(); fileName = "company_wise_report.txt"; break;
            case 9: text = reportService.packageStatistics(); fileName = "package_report.txt"; break;
            default: text = reportService.completePlacementReport(); fileName = ReportFileHandler.PLACEMENT_REPORT_FILE; break;
        }

        System.out.println();
        System.out.println(text);

        if (input.readYesNo("Save this report to the reports folder? (y/n): ")) {
            Path saved = fileHandler.saveReport(fileName, text);
            System.out.println("Saved to " + saved.toAbsolutePath());
        }
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws SQLException, IOException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        } catch (IOException e) {
            System.out.println("\n[!] Could not save the file: " + e.getMessage());
        }
    }

}