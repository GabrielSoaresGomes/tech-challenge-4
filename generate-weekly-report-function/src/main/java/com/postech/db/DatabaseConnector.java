package com.postech.db;

import com.postech.logging.AppLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseConnector {

    private final String url;
    private final String user;
    private final String password;
    private final AppLogger logger;

    public DatabaseConnector(AppLogger logger) {
        this.logger = logger;

        this.url = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/tech_challenge");
        this.user = getEnvOrDefault("DB_USER", "tech_user");
        this.password = getEnvOrDefault("DB_PASSWORD", "tech_password");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Se nem o driver existe, não faz sentido continuar
            logger.error("PostgreSQL driver não encontrado", e);
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

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Conexão com banco estabelecida com sucesso.", Map.of(
                    "dbUrl", url,
                    "dbUser", user
            ));
            return true;
        } catch (SQLException e) {
            logger.error("Falha ao conectar ao banco de dados no startup.", e, Map.of(
                    "dbUrl", url,
                    "dbUser", user
            ));
            return false;
        }
    }
}
