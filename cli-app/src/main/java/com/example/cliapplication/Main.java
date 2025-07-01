package com.example.cliapplication;

import com.example.cliapplication.service.CLI;

/**
 * The entry point for the CLI application.
 */
public class Main {
    /**
     * Creates a new Main instance.
     */
    private Main() {
        // default constructor
    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        CLI cli = new CLI(args);
        cli.run();
    }
}