package com.example;

import java.time.LocalDate;

public class MoonMission {
    private Long missionId;
    private String spacecraft;
    private String missionType;
    private LocalDate launchDate;

    public MoonMission(Long missionId, String spacecraft, String missionType, LocalDate launchDate) {
        this.missionId = missionId;
        this.spacecraft = spacecraft;
        this.missionType = missionType;
        this.launchDate = launchDate;
    }

    public MoonMission() {}

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public String getSpacecraft() {
        return spacecraft;
    }

    public void setSpacecraft(String spacecraft) {
        this.spacecraft = spacecraft;
    }

    public String getMissionType() {
        return missionType;
    }

    public void setMissionType(String missionType) {
        this.missionType = missionType;
    }

    public LocalDate getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }
}
