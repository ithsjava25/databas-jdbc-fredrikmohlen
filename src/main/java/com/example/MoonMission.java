package com.example;

import java.time.LocalDate;

public class MoonMission {
    private Long missionId;
    private String spacecraft;
    private String missionType;
    private LocalDate launchDate;

    /**
     * Creates a MoonMission with the specified identifier, spacecraft name, mission type, and launch date.
     *
     * @param missionId the unique identifier of the mission
     * @param spacecraft the name of the spacecraft used for the mission
     * @param missionType the category or type of the mission (for example, "crewed" or "uncrewed")
     * @param launchDate the launch date of the mission
     */
    public MoonMission(Long missionId, String spacecraft, String missionType, LocalDate launchDate) {
        this.missionId = missionId;
        this.spacecraft = spacecraft;
        this.missionType = missionType;
        this.launchDate = launchDate;
    }

    /**
 * Creates a MoonMission instance with all fields left unset (null).
 */
public MoonMission() {}

    /**
     * Gets the mission identifier.
     *
     * @return the mission identifier, or null if it has not been set
     */
    public Long getMissionId() {
        return missionId;
    }

    /**
     * Sets the mission's identifier.
     *
     * @param missionId the identifier for the mission
     */
    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    /**
     * Gets the name of the spacecraft.
     *
     * @return the spacecraft name, or null if not set
     */
    public String getSpacecraft() {
        return spacecraft;
    }

    /**
     * Sets the name of the spacecraft for this mission.
     *
     * @param spacecraft the spacecraft name (may be null)
     */
    public void setSpacecraft(String spacecraft) {
        this.spacecraft = spacecraft;
    }

    /**
     * Gets the mission type.
     *
     * @return the mission type, or null if it has not been set
     */
    public String getMissionType() {
        return missionType;
    }

    /**
     * Sets the mission type for this MoonMission.
     *
     * @param missionType the mission type or category (for example, "manned", "unmanned", or "orbital")
     */
    public void setMissionType(String missionType) {
        this.missionType = missionType;
    }

    /**
     * Gets the mission's launch date.
     *
     * @return the launch date of the mission, or {@code null} if not set
     */
    public LocalDate getLaunchDate() {
        return launchDate;
    }

    /**
     * Sets the mission's launch date.
     *
     * @param launchDate the launch date to assign to this mission; may be null
     */
    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }
}