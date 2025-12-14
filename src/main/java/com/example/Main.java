package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private final AccountRepository accountRepository = new AccountRepository();
    private final MoonMissionRepository moonMissionRepository = new MoonMissionRepository();

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             Scanner scanner = new Scanner(System.in)) {

            String loggedInUser = authenticateUser(scanner);
            if (loggedInUser == null) {
                System.out.println("Invalid username or password. Exiting");
                return;
            }
            System.out.println("\nWelcome, " + loggedInUser + "!");
            boolean running = true;
            while (running) {
                System.out.println("""
                        
                           Press a number for  next assignment:
                        
                           1) List moon missions.
                           2) Get a moon mission.
                           3) Count missions for a given year.
                           4) Create an account.
                           5) Update an account password.
                           6) Delete an account.
                           0) Exit.
                        """);

                System.out.print("Choose (0-6): ");
                if (!scanner.hasNextLine()) {
                    running = false;
                    break;
                }
                String choiceStr = scanner.nextLine().trim();

                int choice;
                try {
                    choice = Integer.parseInt(choiceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number (0-6).");
                    continue;
                }

                switch (choice) {
                    case 0:
                        System.out.println("Exiting application. Goodbye!");
                        running = false;
                        break;
                    case 1:
                        System.out.println("Executing: 1) List moon missions...");
                        listMoonMissions();
                        break;
                    case 2:
                        System.out.println("Executing: 2) Get a moon mission by mission_id...");
                        moonMissionsById(scanner);
                        break;
                    case 3:
                        System.out.println("Executing: 3) Count missions for a given year...");
                        countingMissionsForAGivenYear(connection, scanner);
                        break;
                    case 4:
                        System.out.println("Executing: 4) Create an account...");
                        createAnAccount(scanner);
                        break;
                    case 5:
                        System.out.println("Executing: 5) Update an account password...");
                        updateAccountPassword(scanner);
                        break;
                    case 6:
                        System.out.println("Executing: 6) Delete an account...");
                        deleteAccount(scanner);
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 0 and 6.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed. " + e.getMessage(), e);
        }
    }


    private String authenticateUser(Scanner scanner) {
        System.out.print("Username: ");
        if (!scanner.hasNextLine()) {
            return null; // Hantera EOF i testmiljön
        }
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        if (!scanner.hasNextLine()) {
            return null; // Hantera EOF i testmiljön
        }
        String password = scanner.nextLine().trim();

        Optional<Account> foundAccount = accountRepository.authenticate(username, password);

        return foundAccount.map(Account::getName).orElse(null);
    }


    private void listMoonMissions() {
        System.out.println("Moon missions: ");

        try {
            List<MoonMission> missions = moonMissionRepository.findAll();

            if (missions.isEmpty()) {
                System.out.println("No moon missions found.");
            } else {
                for (MoonMission mission : missions) {
                    System.out.println(mission.getSpacecraft());
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error listing missions: " + e.getMessage(), e);
        }
    }

    private void moonMissionsById(Scanner scanner) {
        System.out.println("Enter moon mission id: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String missionId = scanner.nextLine().trim();
        long id=0;
        try {
            id = Long.parseLong(missionId);
        } catch (NumberFormatException e) {
        System.out.println("Invalid moon mission id.");
        return;
        }

        try {
            Optional<MoonMission> mission = moonMissionRepository.findById(id);

            if (mission.isPresent()) {
                MoonMission m = mission.get();
                System.out.println("ID: " + m.getMissionId());
                System.out.println("Spacecraft: " + m.getSpacecraft());
                System.out.println("Mission type: " + m.getMissionType());
                System.out.println("Launch date: " + m.getLaunchDate());
            } else  {
                System.out.println("Mission not found.");
            }
        } catch (RuntimeException e) {
            System.out.println("Invalid moon mission id.");
        }
    }

    private void countingMissionsForAGivenYear(Connection connection, Scanner scanner) {
        System.out.println("Enter year: ");
        if (!scanner.hasNextLine()) {
            System.out.println("Invalid year");
            return;
        }
        String yearString = scanner.nextLine().trim();
        int year;
        try {
            year = Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year. Please enter a number (e.g. 1987).");
            return;
        }

        try {
            int count = moonMissionRepository.countByLaunchYear(year);

            if (count > 0) {
                System.out.println("Mission count for year: " + year);
                System.out.println("Number of moon missions: " + count);
            } else {
                System.out.println("No moon missions for year: " + year);
            }
        } catch (RuntimeException e) {
            System.err.println("Error counting missions: " + e.getMessage());
        }
    }

    private void createAnAccount(Scanner scanner) {
        System.out.println("Creating an account...");

        System.out.print("Enter first name: ");
        if (!scanner.hasNextLine()) {return;}
        String firstName = scanner.nextLine().trim();
        if (firstName.length() < 3) {System.out.println("First name must be at least 3 characters long."); return; }

        System.out.print("Enter last name: ");
        if (!scanner.hasNextLine()) {return;}
        String lastName = scanner.nextLine().trim();
        if (lastName.length() < 3) {System.out.println("Last name must be at least 3 characters long."); return; }

        System.out.print("Enter ssn: ");
        if (!scanner.hasNextLine()) {return;}
        String ssn = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        if (!scanner.hasNextLine()) {return;}
        String password = scanner.nextLine().trim();

        String name = firstName.substring(0, 3) + lastName.substring(0, 3);

        Account newAccount = new Account(firstName, lastName, ssn, password, name);
        Account createAccount = accountRepository.createAnAccount(newAccount);

        System.out.println("Successfully created account with ID: " + createAccount.getUserId());

    }

    private void updateAccountPassword(Scanner scanner) {
        System.out.println("Enter user_id: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String userIdStr = scanner.nextLine().trim();
        long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user_id. Please enter a number");
            return;
        }

        System.out.println("Enter new password: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String newPassword = scanner.nextLine();

        try {
            int affedtedRows = accountRepository.updatePassword(userId, newPassword);

            if (affedtedRows > 0) {
                System.out.println("Successfully updated password for user ID: " + userId);
            } else {
                System.out.println("Failed to update password for user ID: " + userId);
            }

        } catch (RuntimeException e) {
            System.out.println("ERROR: Failed to update password for user ID: " + userId);
        }
    }

    private void deleteAccount(Scanner scanner) {
        System.out.println("Enter user id, that you wish to delete: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String userIdStr = scanner.nextLine().trim();
        long userId=0;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user_id. Please enter a number");
        return;
        }

        try {
            int affectedRows = accountRepository.deleteAccount(userId);

            if (affectedRows > 0) {
                System.out.println("Successfully deleted the account with ID: " + userId);
            } else {
                System.out.println("Failed to delete the account. ID " + userId + " not found.");
            }
        } catch (RuntimeException e) {
            System.err.println("ERROR: Failed to delete the account.");
        }
    }


}
