package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class Main {

    /**
     * Application entry point; starts the development database when dev mode is detected and runs the application.
     *
     * @param args command-line arguments (e.g. include "--dev" to enable development mode)
     */
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
         * Read a configuration value, preferring the Java system property and falling back to an environment variable.
         *
         * @param propertyKey the system property key to check first
         * @param envKey the environment variable name to check if the system property is missing or empty
         * @return the trimmed configuration value, or {@code null} if neither source provides a non-empty value
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

    /**
     * Run the interactive application: connect to the configured database, authenticate a user,
     * and enter a menu-driven loop to perform moon-mission queries and account operations.
     *
     * <p>The method reads DB settings from system properties or environment variables,
     * opens a JDBC connection and a System.in Scanner, prompts for credentials, then presents
     * options to list missions, get a mission by id, count missions by year, create/update/delete
     * accounts, or exit.</p>
     *
     * @throws IllegalStateException if required DB configuration (APP_JDBC_URL, APP_DB_USER, APP_DB_PASS)
     *                               is missing
     * @throws RuntimeException     if a database operation fails
     */
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
                        countingMissionsForAGivenYear(scanner);
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


    /**
     * Prompts for username and password from the provided Scanner and returns the authenticated account's display name.
     *
     * @param scanner the input source used to read username and password lines
     * @return the account's name when credentials match an existing account; `null` if authentication fails or input ends before credentials are provided
     */
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


    /**
     * Prints the spacecraft names of all moon missions to standard output.
     *
     * If no missions are found a message indicating that is printed.
     *
     * @throws RuntimeException if retrieving the missions fails
     */
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

    /**
     * Prompts the user for a moon mission id, looks up the mission, and prints its details or an error message.
     * If the input is not a valid number or the lookup fails, prints "Invalid moon mission id.".
     * If no mission is found for the given id, prints "Mission not found.".
     *
     * @param scanner the input Scanner used to read the mission id from the user
     */
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

    /**
     * Prompts the user for a year, queries how many moon missions launched that year, and prints the result.
     *
     * @param scanner the input source used to read the user's responses (e.g., a Scanner over System.in)
     */
    private void countingMissionsForAGivenYear(Scanner scanner) {
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

    /**
     * Interactively gathers user information and creates a new account in the repository.
     *
     * Prompts for first name, last name, SSN, and password; validates that first and last names
     * are at least 3 characters long, constructs an account short name, persists the account via
     * the AccountRepository, and prints the created account's ID. If input is exhausted at any
     * prompt the method returns without creating an account.
     *
     * @param scanner the Scanner to read user input from (used for interactive prompts; may return early on EOF)
     */
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

    /**
     * Prompts for a user id and a new password, updates that account's password, and prints whether the update succeeded.
     *
     * @param scanner the Scanner to read user input from (used to obtain the user id and new password)
     */
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
        String newPassword = scanner.nextLine().trim();

        try {
            int affectedRows = accountRepository.updatePassword(userId, newPassword);

            if (affectedRows > 0) {
                System.out.println("Successfully updated password for user ID: " + userId);
            } else {
                System.out.println("Failed to update password for user ID: " + userId);
            }

        } catch (RuntimeException e) {
            System.out.println("ERROR: Failed to update password for user ID: " + userId);
        }
    }

    /**
     * Prompts for a user id, attempts to delete the corresponding account, and prints the outcome.
     *
     * Reads a line from the provided Scanner; if the input cannot be parsed as a long an informative message is printed.
     * If deletion succeeds a confirmation is printed; if no account was deleted a not-found message is printed.
     * On unexpected runtime errors an error message is written to standard error.
     */
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