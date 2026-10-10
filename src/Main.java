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
import exception.InvalidInputException;
import exception.StudentNotFoundException;
import file.ReportFileHandler;
import java.sql.SQLException;
import java.util.Scanner;
import model.PlacementOfficer;
import model.Student;
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
 * Checks the database, makes sure a default officer and the demo students exist,
 * builds every DAO, service, and menu, and then starts the login screen.
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

    // Demo students, created at startup so the demo data can be used immediately.
    private static final boolean SEED_DEMO_STUDENTS = true;
    private static final String DEMO_STUDENT_PASSWORD = "secret123";

    public static void main(String[] args) {
        if (!DBConnection.testConnection()) {
            System.out.println("\nThe program cannot start without the database.");
            System.out.println("Check that MySQL is running and that db.properties is correct.");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ensureDefaultOfficer();
            if (SEED_DEMO_STUDENTS) {
                ensureDemoStudents();
            }
            LoginMenu loginMenu = buildLoginMenu(new InputUtil(scanner));
            loginMenu.run();
        } catch (SQLException | InvalidInputException | StudentNotFoundException e) {
            System.out.println("\nError during startup: " + e.getMessage());
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
     * Creates a couple of demo students with a basic academic profile so the UI can be tested quickly.
     */
    private static void ensureDemoStudents() throws SQLException, InvalidInputException, StudentNotFoundException {
        StudentDAO studentDAO = new StudentDAO();
        StudentService studentService = new StudentService(studentDAO);

        seedDemoStudent(studentService, studentDAO, "Asha Verma", "asha@mail.com", "9876543210",
                "CSE", 2027, 8.2, 0, 90, 88, "Jaipur", "Java, SQL");
        seedDemoStudent(studentService, studentDAO, "Dev Rao", "dev@mail.com", "9123456780",
                "CSE", 2027, 7.8, 0, 85, 82, "Delhi", "Python, React");
    }

    /**
     * Registers one demo student and fills in the academic details if the email is not already used.
     */
    private static void seedDemoStudent(StudentService studentService, StudentDAO studentDAO,
                                       String name, String email, String phone, String department,
                                       int graduationYear, double cgpa, int backlogs,
                                       double tenthPercentage, double twelfthPercentage,
                                       String address, String skills)
            throws SQLException, InvalidInputException, StudentNotFoundException {
        if (studentDAO.getStudentByEmail(email) != null) {
            return;
        }

        Student student = studentService.registerStudent(name, email, phone, DEMO_STUDENT_PASSWORD);
        student.setDepartment(department);
        student.setCourse("B.Tech");
        student.setGraduationYear(graduationYear);
        student.setCgpa(cgpa);
        student.setBacklogs(backlogs);
        student.setTenthPercentage(tenthPercentage);
        student.setTwelfthPercentage(twelfthPercentage);
        student.setAddress(address);
        student.setSkills(skills);
        studentService.updateProfile(student);
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