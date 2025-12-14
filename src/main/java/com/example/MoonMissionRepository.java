package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoonMissionRepository {

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
