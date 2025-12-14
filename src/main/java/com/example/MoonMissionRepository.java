package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoonMissionRepository {

    /**
     * Map the current row of a ResultSet to a MoonMission instance.
     *
     * The returned MoonMission is populated from the ResultSet columns
     * "mission_id", "spacecraft", "mission_type" and "launch_date".
     * If "launch_date" is SQL NULL, the mission's launchDate will be set to null;
     * otherwise the SQL Date is converted to a java.time.LocalDate.
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a MoonMission populated with values from the current ResultSet row
     * @throws SQLException if an error occurs while reading values from the ResultSet
     */
    private MoonMission mapResultSetToMission(ResultSet rs) throws SQLException {
        MoonMission mission = new MoonMission();
        mission.setMissionId(rs.getLong("mission_id"));
        mission.setSpacecraft(rs.getString("spacecraft"));
        mission.setMissionType(rs.getString("mission_type"));

        Date sqlDate = rs.getDate("launch_date");
        if (sqlDate != null) {
            mission.setLaunchDate(sqlDate.toLocalDate());
        } else {
            mission.setLaunchDate(null);
        }
        return mission;
    }

    /**
     * Retrieve all moon mission records ordered by spacecraft.
     *
     * Each result row is mapped to a MoonMission instance.
     *
     * @return a list of MoonMission objects ordered by spacecraft; an empty list if no records are found
     * @throws RuntimeException if a database access error occurs while querying missions
     */
    public List<MoonMission> findAll() {
        List<MoonMission> missions = new ArrayList<>();
        String sql = " SELECT mission_id, spacecraft, mission_type, launch_date FROM moon_mission ORDER BY spacecraft ";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                missions.add(mapResultSetToMission(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding all moon missions.", e);
        }
        return missions;
    }

    /**
     * Retrieve a moon mission by its ID.
     *
     * @param id the mission_id to look up
     * @return an Optional containing the MoonMission with the specified id if found, otherwise empty
     * @throws RuntimeException if a database error occurs while querying for the mission
     */
    public Optional<MoonMission> findById(long id) {
        String sql = " SELECT mission_id, spacecraft, mission_type, launch_date FROM moon_mission WHERE mission_id = ? ";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMission(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding moon mission by ID: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Counts moon missions whose launch date falls in the specified year.
     *
     * @param year the calendar year to count launches for (e.g., 1969)
     * @return the number of missions launched in the given year
     * @throws RuntimeException if a database error occurs while performing the count
     */
    public int countByLaunchYear(int year) {
        String sql = " SELECT count(*) AS mission_count FROM moon_mission WHERE YEAR(launch_date) = ? ";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("mission_count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting missions for year: " + year, e);
        }
        return 0;
    }
}