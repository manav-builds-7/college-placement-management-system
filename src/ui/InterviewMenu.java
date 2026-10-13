package ui;

import enums.InterviewResult;
import enums.InterviewRound;
import exception.InvalidInputException;
import model.Interview;
import service.InterviewService;
import util.InputUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * The officer's interview screens: schedule rounds, record results, and review them.
 * All rules (order of rounds, one result only, date checks) are in InterviewService.
 */
public class InterviewMenu extends ConsoleMenu {

    private final InputUtil input;
    private final InterviewService interviewService;

    public InterviewMenu(InputUtil input, InterviewService interviewService) {
        super(72);
        this.input = input;
        this.interviewService = interviewService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("MANAGE INTERVIEWS", "Schedule an interview", "Record an interview result",
                    "View interviews for an application", "View all interviews", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 5);

            switch (choice) {
                case 1: runSafely(() -> scheduleInterview()); break;
                case 2: runSafely(() -> recordResult()); break;
                case 3: runSafely(() -> viewForApplication()); break;
                case 4: runSafely(() -> printInterviews(interviewService.getAllInterviews(), "ALL INTERVIEWS")); break;
                default: back = true; break;
            }
        }
    }

    private void scheduleInterview() throws InvalidInputException, SQLException {
        printTitle("SCHEDULE INTERVIEW");
        int applicationId = input.readInt("Application ID: ");
        InterviewRound round = chooseRound();
        LocalDate date = input.readDate("Interview date (yyyy-mm-dd): ");
        LocalTime time = input.readTime("Interview time (HH:mm): ");
        String mode = input.readRequiredString("Mode (e.g. On-campus, Online): ");
        String venue = input.readRequiredString("Venue: ");
        String interviewer = input.readRequiredString("Interviewer name: ");

        Interview interview = new Interview(applicationId, round, date, time, mode, venue, interviewer);
        interviewService.scheduleInterview(interview);
        System.out.println("\nInterview scheduled with ID " + interview.getInterviewId()
                + ". The application status was updated where this round requires it.");
    }

    private void recordResult() throws InvalidInputException, SQLException {
        printTitle("RECORD INTERVIEW RESULT");
        int interviewId = input.readInt("Interview ID: ");
        Interview interview = interviewService.findInterview(interviewId);
        System.out.println("Round " + interview.getRound() + " on " + interview.getDate()
                + ", current result " + interview.getResult());

        int choice = input.readMenuChoice("Result (1 = PASSED, 2 = FAILED): ", 1, 2);
        InterviewResult result = choice == 1 ? InterviewResult.PASSED : InterviewResult.FAILED;
        String remarks = input.readString("Remarks (optional): ");

        interviewService.recordResult(interviewId, result, remarks);
        System.out.println("\nResult recorded as " + result + ".");
    }

    private void viewForApplication() throws InvalidInputException, SQLException {
        int applicationId = input.readInt("Application ID: ");
        printInterviews(interviewService.getInterviewsForApplication(applicationId),
                "INTERVIEWS FOR APPLICATION " + applicationId);
    }

    /** Shows the four rounds as a numbered list and returns the chosen one. */
    private InterviewRound chooseRound() {
        System.out.println("Round:");
        System.out.println("  1. APTITUDE");
        System.out.println("  2. TECHNICAL");
        System.out.println("  3. HR");
        System.out.println("  4. MANAGERIAL");
        int choice = input.readMenuChoice("Choose round: ", 1, 4);
        switch (choice) {
            case 1: return InterviewRound.APTITUDE;
            case 2: return InterviewRound.TECHNICAL;
            case 3: return InterviewRound.HR;
            default: return InterviewRound.MANAGERIAL;
        }
    }

    private void printInterviews(List<Interview> interviews, String title) {
        printTitle(title);
        if (interviews.isEmpty()) {
            System.out.println("No interviews found.");
            return;
        }
        for (Interview interview : interviews) {
            System.out.println("Interview " + interview.getInterviewId()
                    + " | Application " + interview.getApplicationId()
                    + " | " + interview.getRound()
                    + " | " + interview.getDate() + " " + interview.getTime()
                    + " | " + interview.getMode() + ", " + interview.getVenue()
                    + " | " + interview.getInterviewer()
                    + " | " + interview.getResult());
            if (!interview.getRemarks().isEmpty()) {
                System.out.println("    Remarks: " + interview.getRemarks());
            }
        }
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        }
    }

}