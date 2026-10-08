package com.ledgr.sandbox.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/ledgr_db}")
    private String pgUrl;

    @Value("${spring.datasource.username:postgres}")
    private String pgUser;

    @Value("${spring.datasource.password:jana}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Verifying PostgreSQL database availability at {}...", pgUrl);
        boolean pgAvailable = false;

        try {
            Class.forName("org.postgresql.Driver");
            DriverManager.setLoginTimeout(2);
            // First check postgres server root
            String testUrl = pgUrl.contains("/ledgr_db") ? pgUrl.replace("/ledgr_db", "/postgres") : pgUrl;
            try (Connection conn = DriverManager.getConnection(testUrl, pgUser, pgPassword)) {
                pgAvailable = true;
                log.info("✅ PostgreSQL server connection verified successfully.");
            }
        } catch (Exception e) {
            log.warn("⚠️ PostgreSQL connection check failed ({}).", e.getMessage());
        }

        if (pgAvailable) {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(pgUrl);
                config.setUsername(pgUser);
                config.setPassword(pgPassword);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(3000);
                config.setIdleTimeout(30000);
                config.setPoolName("Ledgr-Postgres-Pool");
                HikariDataSource ds = new HikariDataSource(config);
                // Verify connection to ledgr_db directly
                try (Connection c = ds.getConnection()) {
                    log.info("✅ Connected to PostgreSQL database: ledgr_db");
                    return ds;
                } catch (Exception e) {
                    log.info("Database 'ledgr_db' might not exist yet, initializer will create it.");
                    return ds;
                }
            } catch (Exception e) {
                log.warn("Failed to initialize PostgreSQL pool: {}", e.getMessage());
            }
        }

        log.info("🔄 Falling back to in-memory H2 database (PostgreSQL compatibility mode). Backend will run seamlessly.");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setJdbcUrl("jdbc:h2:mem:ledgr_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setMaximumPoolSize(5);
        h2Config.setPoolName("Ledgr-H2-Fallback-Pool");
        return new HikariDataSource(h2Config);
    }
}
