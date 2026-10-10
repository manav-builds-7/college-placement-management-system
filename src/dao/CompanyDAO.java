package dao;

import model.Company;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for companies. Contains only SQL and row mapping. No
 * validation and no business rules.
 */
public class CompanyDAO {

    private static final String BASE_SELECT
            = "SELECT company_id, company_name, industry, location, website, "
            + "       hr_name, hr_email, hr_phone "
            + "FROM companies ";

    // ---------- CREATE ----------
    /**
     * Inserts a company and returns the generated company_id. Also sets the new
     * ID on the object passed in.
     */
    /**
     * Counts placement drives for a company. Used before deletion.
     */
    public int countDrivesForCompany(int companyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM placement_drives WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int addCompany(Company company) throws SQLException {
        String sql = "INSERT INTO companies (company_name, industry, location, website, "
                + "hr_name, hr_email, hr_phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getIndustry());
            ps.setString(3, company.getLocation());
            ps.setString(4, company.getWebsite());
            ps.setString(5, company.getHrName());
            ps.setString(6, company.getHrEmail());
            ps.setString(7, company.getHrPhone());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    company.setCompanyId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new company.");
    }

    // ---------- READ ----------
    /**
     * Returns the company with this ID, or null if none exists.
     */
    public Company getCompanyById(int companyId) throws SQLException {
        String sql = BASE_SELECT + "WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Returns the company with this exact name, or null. Used for the
     * duplicate-name check. The collation is case-insensitive, so "infosys"
     * matches "Infosys".
     */
    public Company getCompanyByName(String companyName) throws SQLException {
        String sql = BASE_SELECT + "WHERE company_name = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, companyName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Returns all companies, ordered by name.
     */
    public List<Company> getAllCompanies() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY company_name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            return readCompanies(ps);
        }
    }

    /**
     * Returns companies whose name, industry, or location contains the keyword.
     */
    public List<Company> searchCompanies(String keyword) throws SQLException {
        String sql = BASE_SELECT
                + "WHERE company_name LIKE ? OR industry LIKE ? OR location LIKE ? "
                + "ORDER BY company_name";
        String pattern = "%" + keyword + "%";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            return readCompanies(ps);
        }
    }

    /**
     * Counts how many jobs a company has. The service calls this before
     * deleting, so it can give a clear message.
     */
    public int countJobsForCompany(int companyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM jobs WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();          // COUNT(*) always returns exactly one row
                return rs.getInt(1);
            }
        }
    }

    // ---------- UPDATE ----------
    /**
     * Saves changes to an existing company. Returns true if a row was updated.
     */
    public boolean updateCompany(Company company) throws SQLException {
        String sql = "UPDATE companies SET company_name = ?, industry = ?, location = ?, "
                + "website = ?, hr_name = ?, hr_email = ?, hr_phone = ? WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, company.getCompanyName());
            ps.setString(2, company.getIndustry());
            ps.setString(3, company.getLocation());
            ps.setString(4, company.getWebsite());
            ps.setString(5, company.getHrName());
            ps.setString(6, company.getHrEmail());
            ps.setString(7, company.getHrPhone());
            ps.setInt(8, company.getCompanyId());
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- DELETE ----------
    /**
     * Deletes a company. Fails with a foreign-key error if it still has jobs,
     * because the schema uses ON DELETE RESTRICT. The service checks first.
     */
    public boolean deleteCompany(int companyId) throws SQLException {
        String sql = "DELETE FROM companies WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------
    private List<Company> readCompanies(PreparedStatement ps) throws SQLException {
        List<Company> companies = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                companies.add(mapRow(rs));
            }
        }
        return companies;
    }

    /**
     * The only place that maps company columns to the Company object.
     */
    private Company mapRow(ResultSet rs) throws SQLException {
        return new Company(
                rs.getInt("company_id"),
                rs.getString("company_name"),
                rs.getString("industry"),
                rs.getString("location"),
                rs.getString("website"),
                rs.getString("hr_name"),
                rs.getString("hr_email"),
                rs.getString("hr_phone"));
    }
}
