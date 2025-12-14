package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    /**
     * Resolve a configuration value by checking a system property first and an environment variable second.
     *
     * @param propertyKey the system property key to check first
     * @param envKey      the environment variable name to check if the system property is missing or blank
     * @return            the trimmed configuration value if present and not blank, or {@code null} if missing
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    /**
     * Obtain a JDBC Connection using configuration resolved from system properties or environment variables.
     *
     * <p>Configuration keys consulted: {@code APP_JDBC_URL}, {@code APP_DB_USER}, {@code APP_DB_PASS}.
     *
     * @return a JDBC {@link Connection} created with the resolved JDBC URL, username, and password
     * @throws IllegalStateException if any of the required configuration values is missing
     * @throws SQLException if a database access error occurs while creating the connection
     */
    public static Connection getConnection() throws SQLException {
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }
}