package com.example.cliapplication.model;

/**
 * Defines the contract for creating a request body from a given set of arguments.
 *
 * Implementations of this interface are responsible for formatting the arguments
 * into a specific request body format (e.g., JSON, XML, etc.).
 */
public interface RequestBodyCreator {

    /**
     * Creates a request body string based on the provided arguments.
     * The format of the request body is determined by the implementing class.
     *
     * @param arguments The arguments to be included in the request body.
     * @return A string representing the formatted request body.
     */
    String createRequestBody(String arguments);
}