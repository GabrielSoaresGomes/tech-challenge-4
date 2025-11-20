package com.postech.repository;

import com.postech.domain.Feedback;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {

    private final String url;
    private final String user;
    private final String password;

    public FeedbackRepository() {
        this.url = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/tech_challenge");
        this.user = getEnvOrDefault("DB_USER", "tech_user");
        this.password = getEnvOrDefault("DB_PASSWORD", "tech_password");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver não encontrado", e);
        }
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public List<Feedback> findBetween(LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT id, description, note, urgency, send_date
                FROM feedbacks
                WHERE send_date BETWEEN ? AND ?
                ORDER BY send_date
                """;

        List<Feedback> result = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback f = new Feedback();
                    f.id = rs.getLong("id");
                    f.description = rs.getString("description");
                    f.note = rs.getInt("note");
                    f.urgency = rs.getBoolean("urgency");
                    f.sendDate = rs.getTimestamp("send_date").toLocalDateTime();
                    result.add(f);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar feedbacks", e);
        }

        return result;
    }
}
