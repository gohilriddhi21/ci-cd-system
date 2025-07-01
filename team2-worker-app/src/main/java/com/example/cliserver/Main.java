package com.example.cliserver;

import com.example.cliserver.backend.messaging.PipelineConsumer;

/**
 * Main entry point for the pipeline tool application.
 * Provides worker mode for processing pipeline requests and CLI mode for direct commands.
 */
public class Main {
    /**
     * default constructor
     */
    private Main() {
        // default constructor
    }
    /**
     * Starts the application in either worker or CLI mode based on arguments.
     * Initializes MongoDB connection and handles startup errors.
     *
     * @param args command-line arguments. "worker" as first arg starts worker mode.
     */
    public static void main(String[] args) {
        // Check if we should run in worker mode
        boolean workerMode = args.length > 0 && args[0].equals("worker");

        try {
            // Start in worker mode
            System.out.println("Starting in worker mode...");
            PipelineConsumer consumer = new PipelineConsumer();
            consumer.start();

            // Keep the main thread alive
            try {
                System.out.println("Worker is running. Press Ctrl+C to exit.");
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                consumer.shutdownExecutor();
            }

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}