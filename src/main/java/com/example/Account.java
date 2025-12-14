package com.example;

public class Account {
    private Long userId; // primary key
    private String firstName;
    private String lastName;
    private String ssn;
    private String password;
    private String name;

    /**
 * Constructs a new Account with all fields left at their default values.
 */
public Account() {}
    /**
     * Creates a new Account initialized with the given personal and credential fields.
     *
     * @param firstName the account holder's first name
     * @param lastName  the account holder's last name
     * @param ssn       the account holder's social security number
     * @param password  the account's password
     * @param name      the display name for the account
     */
    public Account(String firstName, String lastName, String ssn, String password, String name) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.password = password;
        this.name = name;
    }

    /**
     * Gets the account's user identifier.
     *
     * @return the user identifier, or null if it has not been assigned
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the account's primary key identifier.
     *
     * @param userId the unique identifier for this account (primary key)
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the account's first name.
     *
     * @return the account's first name, or null if not set
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the account's first name.
     *
     * @param firstName the first name to assign to the account
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the account's last name.
     *
     * @return the account's last name, possibly {@code null}
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the account's last name.
     *
     * @param lastName the new last name for the account
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the account's Social Security number.
     *
     * @return the Social Security number
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Sets the account's Social Security Number.
     *
     * @param ssn the Social Security Number to assign to this account
     */
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    /**
     * Retrieves the account's password.
     *
     * @return the account's password, or {@code null} if not set
     */
    public String getPassword() {
        return password;
    }

    /**
     * Assigns the account password.
     *
     * @param password the password to associate with the account; no validation or hashing is performed
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieves the account's name.
     *
     * @return the account's name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the account's name.
     *
     * @param name the new name for the account
     */
    public void setName(String name) {
        this.name = name;
    }
}