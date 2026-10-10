package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates JDBC connections to the MySQL database.
 * Every DAO gets its connection from here.
 *
 * The class has no fields and no objects are created from it, so there is
 * no shared static state. It reads db.properties each time a connection
 * is requested, which is slightly slower but keeps the code simple.
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    // Private constructor: this is a utility class, not an object.
    private DBConnection() {
    }

    /**
     * Opens a new connection to MySQL.
     * The caller must close it, usually with try-with-resources.
     *
     * @throws SQLException if the settings are wrong or MySQL is not running
     */
    public static Connection getConnection() throws SQLException {
        Properties config = loadConfig();
        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks that the database can be reached.
     * Used at startup and in tests. Prints a friendly message either way.
     *
     * @return true if a connection could be opened and closed
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Database connection successful.");
            return true;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /** Reads db.properties from the project root. */
    private static Properties loadConfig() throws SQLException {
        Properties config = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
        } catch (IOException e) {
            // A missing config file is reported as a connection problem,
            // so the UI can show a single "database unavailable" message.
            throw new SQLException("Could not read " + CONFIG_FILE + ": " + e.getMessage(), e);
        }
        return config;
    }
}