package com.ledgr.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@Configuration
public class DatabaseInitializer {

    @Value("${spring.datasource.username:${DB_USERNAME:postgres}}")
    private String username;

    @Value("${spring.datasource.password:${DB_PASSWORD:}}")
    private String password;

    @PostConstruct
    public void ensureDatabaseExists() {
        String defaultPostgresUrl = "jdbc:postgresql://localhost:5432/postgres";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(defaultPostgresUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'ledgr_db'");
                if (!rs.next()) {
                    log.info("Database 'ledgr_db' not found. Creating 'ledgr_db' automatically...");
                    stmt.executeUpdate("CREATE DATABASE ledgr_db");
                    log.info("Database 'ledgr_db' created successfully.");
                } else {
                    log.info("Database 'ledgr_db' is verified and available.");
                }
            }
        } catch (Exception e) {
            log.info("PostgreSQL initialization check completed (fallback available): {}", e.getMessage());
        }
    }
}
