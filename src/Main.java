import dao.ApplicationDAO;
import dao.CompanyDAO;
import dao.InterviewDAO;
import dao.JobDAO;
import dao.PlacementDAO;
import dao.PlacementDriveDAO;
import dao.PlacementOfficerDAO;
import dao.ResumeDAO;
import dao.StatisticsDAO;
import dao.StudentDAO;
import file.ReportFileHandler;
import java.sql.SQLException;
import java.util.Scanner;
import model.PlacementOfficer;
import service.ApplicationService;
import service.AuthService;
import service.CompanyService;
import service.InterviewService;
import service.JobService;
import service.PlacementDriveService;
import service.PlacementService;
import service.ReportService;
import service.ResumeService;
import service.StudentService;
import ui.LoginMenu;
import ui.OfficerMenu;
import ui.StudentMenu;
import util.DBConnection;
import util.InputUtil;
import util.PasswordUtil;

/**
 * Entry point of the College Placement Management System.
 * Checks the database, makes sure a default officer exists, builds every
 * DAO, service, and menu, and then starts the login screen.
 * No business rules live here.
 */
public class Main {

    // Default officer account, created only when no such account exists yet.
    // Change the password after the first login in a real deployment.
    private static final String DEFAULT_OFFICER_NAME = "Default Officer";
    private static final String DEFAULT_OFFICER_EMAIL = "admin@college.edu";
    private static final String DEFAULT_OFFICER_PASSWORD = "admin123";
    private static final String DEFAULT_OFFICER_PHONE = "9000000000";
    private static final String DEFAULT_OFFICER_EMPLOYEE_ID = "EMP-000";
    private static final String DEFAULT_OFFICER_DESIGNATION = "Placement Officer";

    public static void main(String[] args) {
        // 1. Stop early with a clear message if the database cannot be reached
        if (!DBConnection.testConnection()) {
            System.out.println("\nThe program cannot start without the database.");
            System.out.println("Check that MySQL is running and that db.properties is correct.");
            return;
        }

        // The Scanner is closed automatically when the program ends
        try (Scanner scanner = new Scanner(System.in)) {
            ensureDefaultOfficer();
            LoginMenu loginMenu = buildLoginMenu(new InputUtil(scanner));
            loginMenu.run();
        } catch (SQLException e) {
            System.out.println("\nDatabase error during startup: " + e.getMessage());
        }
    }

    /**
     * Creates the default officer account if no officer has this email yet.
     * Running the program again does not create a duplicate.
     */
    private static void ensureDefaultOfficer() throws SQLException {
        PlacementOfficerDAO officerDAO = new PlacementOfficerDAO();
        if (officerDAO.getOfficerByEmail(DEFAULT_OFFICER_EMAIL) != null) {
            return;
        }

        PlacementOfficer officer = new PlacementOfficer(0, DEFAULT_OFFICER_NAME, DEFAULT_OFFICER_EMAIL,
                PasswordUtil.hashPassword(DEFAULT_OFFICER_PASSWORD), DEFAULT_OFFICER_PHONE,
                DEFAULT_OFFICER_EMPLOYEE_ID, DEFAULT_OFFICER_DESIGNATION);
        officerDAO.addOfficer(officer);
        System.out.println("Default officer account created: " + DEFAULT_OFFICER_EMAIL
                + " / " + DEFAULT_OFFICER_PASSWORD);
    }

    /**
     * Builds every object in dependency order and returns the login screen.
     * Each line says which objects it needs, so the wiring is easy to follow.
     */
    private static LoginMenu buildLoginMenu(InputUtil input) {
        // ----- DAOs: only these touch the database -----
        StudentDAO studentDAO = new StudentDAO();
        CompanyDAO companyDAO = new CompanyDAO();
        JobDAO jobDAO = new JobDAO();
        PlacementDriveDAO driveDAO = new PlacementDriveDAO();
        ResumeDAO resumeDAO = new ResumeDAO();
        ApplicationDAO applicationDAO = new ApplicationDAO();
        InterviewDAO interviewDAO = new InterviewDAO();
        PlacementDAO placementDAO = new PlacementDAO();
        PlacementOfficerDAO officerDAO = new PlacementOfficerDAO();
        StatisticsDAO statisticsDAO = new StatisticsDAO();

        // ----- Services: built in dependency order -----
        StudentService studentService = new StudentService(studentDAO);
        CompanyService companyService = new CompanyService(companyDAO);
        JobService jobService = new JobService(jobDAO, companyService);
        PlacementDriveService driveService = new PlacementDriveService(driveDAO, companyService, jobService);
        ResumeService resumeService = new ResumeService(resumeDAO, studentService);
        ApplicationService applicationService = new ApplicationService(applicationDAO, studentService, jobService);
        InterviewService interviewService = new InterviewService(interviewDAO, applicationService);
        PlacementService placementService = new PlacementService(placementDAO, applicationService,
                interviewService, studentService, jobService);
        ReportService reportService = new ReportService(statisticsDAO, studentService, companyService,
                jobService, driveService, applicationService, interviewService, placementService);
        AuthService authService = new AuthService(studentService, officerDAO);
        ReportFileHandler fileHandler = new ReportFileHandler();

        // ----- Menus: built last, because they depend on the services above -----
        StudentMenu studentMenu = new StudentMenu(input, studentService, resumeService, jobService,
                companyService, driveService, applicationService, interviewService, placementService,
                fileHandler);
        OfficerMenu officerMenu = new OfficerMenu(input, companyService, jobService, driveService,
                studentService, resumeService, applicationService, interviewService, placementService,
                reportService, fileHandler);

        return new LoginMenu(input, authService, studentService, studentMenu, officerMenu);
    }
}