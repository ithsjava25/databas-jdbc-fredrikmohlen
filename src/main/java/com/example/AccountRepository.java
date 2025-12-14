package com.example;

import java.sql.*;
import java.util.Optional;
import java.util.Scanner;

public class AccountRepository {

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

