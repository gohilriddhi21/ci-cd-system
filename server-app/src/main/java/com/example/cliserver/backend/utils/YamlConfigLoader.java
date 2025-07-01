package com.example.cliserver.backend.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * A Singleton utility class for loading configurations
 * from a YAML file.
 */
public class YamlConfigLoader {

    // Configuration data loaded from the YAML file
    private static volatile YamlConfigLoader instance;

    // Configuration data map
    private static Map<String, Object> configData;

    // Private constructor if decided to initialize in Main
    private YamlConfigLoader() {
        try {
            loadConfig();
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
        }
    }

    /**
     * Returns the Singleton instance of YamlConfigLoader.
     * Initializes the instance lazily if it's not created already.
     *
     * @return The instance of YamlConfigLoader.
     */
    public static YamlConfigLoader getInstance() {
        if (instance == null) {
            synchronized (YamlConfigLoader.class) {
                if (instance == null) {
                    instance = new YamlConfigLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Loads the YAML file once and caches the data.
     *
     * @throws IOException If the file cannot be read.
     */
    private static void loadConfig() throws IOException {
        try (InputStream inputStream = new FileInputStream(Constants.CONFIG_FILE_PATH)) {
            Yaml yaml = new Yaml();
            configData = yaml.load(inputStream);

            if (configData == null || configData.isEmpty()) {
                throw new IOException("YAML file is empty or invalid.");
            }
        } catch (IOException e) {
            throw new IOException("Error loading YAML file at path " +
                    Constants.CONFIG_FILE_PATH + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves a nested value from the loaded YAML config.
     *
     * @param keys The path to the desired key
     * @return The value as a String or null if not found.
     */
    public static String getConfigValue(String... keys) {
        if (configData == null) {
            try {
                loadConfig();
            } catch (IOException e) {
                throw new RuntimeException("Configuration loading failed: " + e.getMessage());
            }
        }

        Map<String, Object> currentMap = configData;
        for (int i = 0; i < keys.length - 1; i++) {
            Object value = currentMap.get(keys[i]);
            if (!(value instanceof Map)) {
                return null;
            }
            currentMap = (Map<String, Object>) value;
        }
        return (String) currentMap.get(keys[keys.length - 1]);
    }
}
