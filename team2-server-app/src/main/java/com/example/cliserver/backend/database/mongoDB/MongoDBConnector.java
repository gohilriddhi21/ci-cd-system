package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.utils.YamlConfigLoader;

import java.io.IOException;

/**
 * A utility class for reading MongoDB configuration details from a YAML file. The class ensures
 * immutability and proper exception handling.
 */
public final class MongoDBConnector {
    /**
     * The MongoDB connection URI.
     */
    private final String uri;
    /**
     * The MongoDB database name.
     */
    private final String database;
    /**
     * The MongoDB collection name.
     */
    private final String collection;

    /**
     * Private constructor to enforce the use of the factory method.
     *
     * @param uri        The MongoDB connection URI.
     * @param database   The MongoDB database name.
     * @param collection The MongoDB collection name.
     */
    private MongoDBConnector(String uri, String database, String collection) {
        this.uri = uri;
        this.database = database;
        this.collection = collection;
    }

    /**
     * Factory method to create a MongoDBConnector instance from a YAML configuration file.
     *
     * @return A fully initialized MongoDBConnector instance.
     * @throws IOException If the file cannot be read or is invalid.
     */
    public static MongoDBConnector loadMongoConfig() throws IOException {
        String uri = YamlConfigLoader.getConfigValue("mongodb", "uri");
        String database = YamlConfigLoader.getConfigValue("mongodb", "database");
        String collection = YamlConfigLoader.getConfigValue("mongodb", "collection");

        if (uri == null || database == null || collection == null) {
            throw new IOException("Missing required MongoDB configuration values.");
        }

        return new MongoDBConnector(uri, database, collection);
    }

    /**
     * Gets the MongoDB connection URI.
     *
     * @return The MongoDB connection URI.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the MongoDB database name.
     *
     * @return The MongoDB database name.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Gets the MongoDB collection name.
     *
     * @return The MongoDB collection name.
     */
    public String getCollection() {
        return collection;
    }
}
