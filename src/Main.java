import java.util.Scanner;
import model.Admin;
import model.Recruiter;
import model.Resume;
import model.Student;


class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to College Placement Portal");
        System.out.println("Please select your role:");
        System.out.println("1. Student");
        System.out.println("2. Recruiter");
        System.out.println("3. Admin");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                registerStudent(scanner);
                break;
            case 2:
                registerRecruiter(scanner);
                break;
            case 3:
                registerAdmin(scanner);
                break;
            default:
                System.out.println("Please select a role from 1 to 3.");
        }
        scanner.close();
    }

    // Register for student profile with CGPA validation and optional resume skills
    private static void registerStudent(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Student ID: ");
        String studentId = scanner.nextLine();
        System.out.print("CGPA (0-10): ");
        if (!scanner.hasNextDouble()) {
            System.out.println("CGPA must be a number from 0 to 10.");
            return;
        }

        double cgpa = scanner.nextDouble();
        if (cgpa < 0 || cgpa > 10) {
            System.out.println("CGPA must be a number from 0 to 10.");
            return;
        }
        scanner.nextLine();

        Student student = new Student(email, username, password, studentId, cgpa);
        System.out.print("Resume skills (optional): ");
        String skills = scanner.nextLine();
        student.setResume(new Resume(skills, "", "", "", "", "", email, username, password, studentId, cgpa));

        System.out.println("Student profile created for " + student.getUsername()
                + " (ID: " + student.getStudentId() + ").");
    }

    // Register for recruiter profile
    private static void registerRecruiter(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Company Name: ");
        String companyName = scanner.nextLine();
        System.out.print("Company Address: ");
        String companyAddress = scanner.nextLine();

        Recruiter recruiter = new Recruiter(email, username, password, companyName, companyAddress);
        System.out.println("Recruiter profile created for " + recruiter.getUsername()
                + " (Company: " + recruiter.getCompanyName() + ").");
    }

    // Register for admin profile
    private static void registerAdmin(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Admin ID: ");
        String adminId = scanner.nextLine();

        Admin admin = new Admin(email, username, password, adminId);
        System.out.println("Admin profile created for " + admin.getUsername()
                + " (ID: " + admin.getAdminId() + ").");
    }
}
