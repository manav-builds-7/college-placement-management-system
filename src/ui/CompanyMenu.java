package ui;

import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import model.Company;
import service.CompanyService;
import util.InputUtil;

import java.sql.SQLException;
import java.util.List;

/** The officer's company management screens: add, update, delete, view, and search. */
public class CompanyMenu extends ConsoleMenu {

    private final InputUtil input;
    private final CompanyService companyService;

    public CompanyMenu(InputUtil input, CompanyService companyService) {
        super(64);
        this.input = input;
        this.companyService = companyService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("MANAGE COMPANIES", "Add company", "Update company", "Delete company",
                    "View all companies", "Search companies", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 6);
            switch (choice) {
                case 1: runSafely(() -> addCompany()); break;
                case 2: runSafely(() -> updateCompany()); break;
                case 3: runSafely(() -> deleteCompany()); break;
                case 4: runSafely(() -> viewCompanies(companyService.getAllCompanies(), "ALL COMPANIES")); break;
                case 5: runSafely(() -> searchCompanies()); break;
                default: back = true; break;
            }
        }
    }

    private void addCompany() throws InvalidInputException, SQLException {
        printTitle("ADD COMPANY");
        Company company = new Company(
                input.readRequiredString("Company name: "),
                input.readRequiredString("Industry: "),
                input.readRequiredString("Location: "),
                input.readString("Website (optional, press Enter to skip): ").trim(),
                input.readRequiredString("HR name: "),
                input.readRequiredString("HR email: "),
                input.readRequiredString("HR phone (10 digits): "));

        companyService.addCompany(company);
        System.out.println("\nCompany added with ID " + company.getCompanyId() + ".");
    }

    private void updateCompany() throws InvalidInputException, CompanyNotFoundException, SQLException {
        int companyId = input.readInt("Company ID to update: ");
        Company company = companyService.findCompany(companyId);

        printTitle("UPDATE COMPANY");
        System.out.println("Press Enter to keep the value shown in brackets.\n");
        company.setCompanyName(input.readOptionalString("Company name", company.getCompanyName()));
        company.setIndustry(input.readOptionalString("Industry", company.getIndustry()));
        company.setLocation(input.readOptionalString("Location", company.getLocation()));
        company.setWebsite(input.readOptionalString("Website", blankIfNull(company.getWebsite())));
        company.setHrName(input.readOptionalString("HR name", company.getHrName()));
        company.setHrEmail(input.readOptionalString("HR email", company.getHrEmail()));
        company.setHrPhone(input.readOptionalString("HR phone", company.getHrPhone()));

        companyService.updateCompany(company);
        System.out.println("\nCompany updated.");
    }

    private void deleteCompany() throws InvalidInputException, CompanyNotFoundException, SQLException {
        int companyId = input.readInt("Company ID to delete: ");
        Company company = companyService.findCompany(companyId);

        if (!input.readYesNo("Delete " + company.getCompanyName() + "? (y/n): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        companyService.deleteCompany(companyId);
        System.out.println("\nCompany deleted.");
    }

    private void searchCompanies() throws SQLException {
        String keyword = input.readRequiredString("Search by name, industry or location: ");
        viewCompanies(companyService.searchCompanies(keyword), "SEARCH RESULTS");
    }

    private void viewCompanies(List<Company> companies, String title) {
        printTitle(title);
        if (companies.isEmpty()) {
            System.out.println("No companies found.");
            return;
        }
        System.out.println(String.format("%-5s %-20s %-14s %-14s %-14s %s",
                "ID", "Company", "Industry", "Location", "HR", "HR phone"));
        System.out.println("-".repeat(width));
        for (Company company : companies) {
            System.out.println(String.format("%-5d %-20s %-14s %-14s %-14s %s",
                    company.getCompanyId(), company.getCompanyName(), company.getIndustry(),
                    company.getLocation(), company.getHrName(), company.getHrPhone()));
        }
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, CompanyNotFoundException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | CompanyNotFoundException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        }
    }

}