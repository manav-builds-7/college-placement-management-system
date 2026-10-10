package service;

import dao.CompanyDAO;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import model.Company;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for companies: unique names, valid HR contact details, and no
 * deletion while jobs still belong to the company.
 */
public class CompanyService {

    private final CompanyDAO companyDAO;

    public CompanyService(CompanyDAO companyDAO) {
        this.companyDAO = companyDAO;
    }

    /**
     * Adds a new company after checking every field and the name rule.
     *
     * @return the saved company, with its new ID filled in
     */
    public Company addCompany(Company company) throws InvalidInputException, SQLException {
        validateCompany(company);

        if (companyDAO.getCompanyByName(company.getCompanyName()) != null) {
            throw new InvalidInputException("A company named " + company.getCompanyName() + " already exists.");
        }

        companyDAO.addCompany(company);
        return company;
    }

    /**
     * Saves changes to an existing company. The name must not clash with
     * another company.
     */
    public void updateCompany(Company company)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        findCompany(company.getCompanyId());
        validateCompany(company);

        Company sameName = companyDAO.getCompanyByName(company.getCompanyName());
        if (sameName != null && sameName.getCompanyId() != company.getCompanyId()) {
            throw new InvalidInputException("A company named " + company.getCompanyName() + " already exists.");
        }

        companyDAO.updateCompany(company);
    }

    /**
     * Deletes a company only when it has no jobs and no placement drives.
     * Drives stay on record even after cancellation, so any drive blocks
     * deletion.
     */
    public void deleteCompany(int companyId)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        Company company = findCompany(companyId);

        int jobCount = companyDAO.countJobsForCompany(companyId);
        if (jobCount > 0) {
            throw new InvalidInputException(company.getCompanyName() + " has " + jobCount
                    + " job(s). Delete them first.");
        }

        int driveCount = companyDAO.countDrivesForCompany(companyId);
        if (driveCount > 0) {
            throw new InvalidInputException(company.getCompanyName() + " has " + driveCount
                    + " placement drive(s) on record and cannot be deleted.");
        }

        companyDAO.deleteCompany(companyId);
    }

    /**
     * Finds a company by ID, or throws if the ID is invalid or unknown.
     */
    public Company findCompany(int companyId)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        ValidationUtil.requirePositiveId(companyId, "Company ID");
        Company company = companyDAO.getCompanyById(companyId);
        if (company == null) {
            throw new CompanyNotFoundException("No company found with ID " + companyId + ".");
        }
        return company;
    }

    public List<Company> getAllCompanies() throws SQLException {
        return companyDAO.getAllCompanies();
    }

    public List<Company> searchCompanies(String keyword) throws SQLException {
        return companyDAO.searchCompanies(keyword == null ? "" : keyword.trim());
    }

    // ---------- Private helpers ----------
    /**
     * The shared rules used by both add and update.
     */
    private void validateCompany(Company company) throws InvalidInputException {
        ValidationUtil.requireNotBlank(company.getCompanyName(), "Company name");
        ValidationUtil.requireNotBlank(company.getIndustry(), "Industry");
        ValidationUtil.requireNotBlank(company.getLocation(), "Location");
        ValidationUtil.requireOptionalUrl(company.getWebsite(), "Website");
        ValidationUtil.requireNotBlank(company.getHrName(), "HR name");
        ValidationUtil.requireEmail(company.getHrEmail(), "HR email");
        ValidationUtil.requirePhone(company.getHrPhone(), "HR phone");
    }
}
