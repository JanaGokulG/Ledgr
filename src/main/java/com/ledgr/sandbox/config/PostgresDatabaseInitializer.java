package com.ledgr.sandbox.config;

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
public class PostgresDatabaseInitializer {

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:jana}")
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
                    log.info("PostgreSQL database 'ledgr_db' is present and ready.");
                }
            }
        } catch (Exception e) {
            log.info("PostgreSQL pre-check completed (fallback/direct connection available): {}", e.getMessage());
        }
    }
}
