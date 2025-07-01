package com.example.cliserver.backend.database.mongoDB;

/**
 * Factory for creating PipelineRunsDao instances.
 */
public class PipelineRunsDaoFactory {

    private static class InstanceHolder {
        private static PipelineRunsDao INSTANCE = null;
    }

    // Private constructor to prevent instantiation
    private PipelineRunsDaoFactory() {
    }

    /**
     * Returns a singleton instance of the PipelineRunsDao.
     *
     * @return a new instance of PipelineRunsDao
     */
    public static synchronized PipelineRunsDao getInstance() {
        if (InstanceHolder.INSTANCE == null) {
            InstanceHolder.INSTANCE = MongoDBPipelineRunsDao.getInstance();
        }
        return InstanceHolder.INSTANCE;
    }
}