package com.ledgr.config;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads .env environment variables into Java System properties
 * following 12-Factor App design principles.
 */
@Slf4j
public class DotenvLoader {

    private static final Map<String, String> ENV_CACHE = new HashMap<>();
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;

        Path envPath = findEnvFile();
        if (envPath == null || !Files.exists(envPath)) {
            log.info(".env file not found at project root. Relying on system environment variables.");
            loaded = true;
            return;
        }

        log.info("Loading environment configuration from: {}", envPath.toAbsolutePath());
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();

                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }

                    ENV_CACHE.put(key, val);
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, val);
                    }
                    count++;
                }
            }
            log.info("Successfully loaded {} environment variables from .env", count);
        } catch (IOException e) {
            log.warn("Could not read .env file: {}", e.getMessage());
        }
        loaded = true;
    }

    public static String get(String key, String defaultValue) {
        if (!loaded) load();
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) return sysVal;

        String envVal = System.getenv(key);
        if (envVal != null && !envVal.isBlank()) return envVal;

        return ENV_CACHE.getOrDefault(key, defaultValue);
    }

    private static Path findEnvFile() {
        Path direct = Paths.get(".env");
        if (Files.exists(direct)) return direct;

        Path parent = Paths.get("..", ".env");
        if (Files.exists(parent)) return parent;

        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            Path fromUserDir = Paths.get(userDir, ".env");
            if (Files.exists(fromUserDir)) return fromUserDir;
        }
        return null;
    }
}
