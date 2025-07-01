package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.utils.YamlConfigLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MongoDBConnectorTest {

    @Test
    void testLoadMongoConfig_success() throws IOException {
        try (MockedStatic<YamlConfigLoader> mockedStatic = org.mockito.Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("mongodb", "uri"))
                    .thenReturn("mongodb://localhost:27017");
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("mongodb", "database"))
                    .thenReturn("test_db");
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("mongodb", "collection"))
                    .thenReturn("test_collection");

            MongoDBConnector connector = MongoDBConnector.loadMongoConfig();

            assertEquals("mongodb://localhost:27017", connector.getUri());
            assertEquals("test_db", connector.getDatabase());
            assertEquals("test_collection", connector.getCollection());
        }
    }

    @Test
    void testLoadMongoConfig_missingValues_throwsIOException() {
        try (MockedStatic<YamlConfigLoader> mockedStatic = org.mockito.Mockito.mockStatic(YamlConfigLoader.class)) {
            mockedStatic.when(() -> YamlConfigLoader.getConfigValue("mongodb", "uri"))
                    .thenReturn(null);

            IOException exception = assertThrows(IOException.class, MongoDBConnector::loadMongoConfig);
            assertEquals("Missing required MongoDB configuration values.", exception.getMessage());
        }
    }
}