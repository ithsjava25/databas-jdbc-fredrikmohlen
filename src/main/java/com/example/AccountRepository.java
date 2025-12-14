package com.example;

import java.sql.*;
import java.util.Optional;
import java.util.Scanner;

public class AccountRepository {

    /**
     * Create an Account populated from the current row of a ResultSet.
     *
     * Maps the columns `user_id`, `name`, `last_name`, `first_name`, `ssn`, and `password`
     * into a new Account instance.
     *
     * @param rs the ResultSet positioned at a row containing account columns
     * @return an Account populated with values from the current ResultSet row
     * @throws SQLException if a database access error occurs while reading columns
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setUserId(rs.getLong("user_id"));
        account.setName(rs.getString("name"));
        account.setLastName(rs.getString("last_name"));
        account.setFirstName(rs.getString("first_name"));
        account.setSsn(rs.getString("ssn"));
        account.setPassword(rs.getString("password"));
        return account;
    }
    /**
     * Authenticate a user by username and password and return the matching account when credentials match.
     *
     * @param username the account name to look up (maps to the `name` column)
     * @param password the password to validate against the stored password
     * @return an Optional containing the authenticated Account if credentials match, or Optional.empty() if not
     */
    public Optional<Account> authenticate(String username, String password) {

        String sql = " SELECT user_id, first_name, last_name, ssn, password, name FROM account WHERE name = ? AND password = ? ";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during account authentication.", e);
        }
        return Optional.empty();
    }

    /**
     * Inserts a new account into the database and sets the generated user ID on the given Account.
     *
     * @param account the Account to persist; its `userId` will be populated with the generated ID
     * @return the same Account instance with `userId` set to the database-generated value
     */
    public Account createAnAccount(Account account) {

        String sql = "INSERT INTO account (first_name, last_name, ssn, password, name) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getFirstName());
            stmt.setString(2, account.getLastName());
            stmt.setString(3, account.getSsn());
            stmt.setString(4, account.getPassword());
            stmt.setString(5, account.getName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating account failed. No rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    account.setUserId(generatedId);
                }else  {
                    throw new SQLException("Creating account failed, no ID obtained");
                }
            }
            return account;

        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed. " + e.getMessage(), e);
        }
    }

    /**
     * Update the account password for the specified user ID.
     *
     * @param userId the user ID of the account to update
     * @param newPassword the new password to set for the account
     * @return the number of rows affected by the update
     * @throws RuntimeException if a database error occurs during the update
     */
    public int updatePassword(long userId, String newPassword) {

        String sql = " UPDATE account SET password = ? WHERE user_id = ? ";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setLong(2, userId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error during password update for user ID: " + userId, e);
        }
    }

    /**
     * Delete the account identified by the given user ID.
     *
     * @param userId the account's primary key (`user_id`) to delete
     * @return the number of rows deleted (0 if no account matched)
     * @throws RuntimeException if a database error occurs during deletion
     */
    public int deleteAccount(long userId) {

        String sql = " DELETE FROM account WHERE user_id = ? ";

        try (Connection connection = ConnectionFactory.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            return stmt.executeUpdate();

    } catch (SQLException e) {
            throw new RuntimeException("Database error during account deletion for user ID: " + userId, e);
        }
    }
}
