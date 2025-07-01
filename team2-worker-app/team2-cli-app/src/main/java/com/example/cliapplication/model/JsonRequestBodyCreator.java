package com.example.cliapplication.model;

/**
 * Implements the {@link RequestBodyCreator} interface to create
 * a JSON formatted request body.
 * The created JSON body contains a single field named "Command"
 * with the provided arguments as its value.
 */
public class JsonRequestBodyCreator implements RequestBodyCreator {

    /**
     * Default Constructor
     */
    public JsonRequestBodyCreator() {
    }

    /**
     * Creates a JSON formatted request body with the given arguments.
     * The JSON body will have the structure: `{"Command":"[arguments]"}`.
     * If an error occurs during the creation of the request body, an error JSON
     * will be returned with details about the error.
     *
     * @param arguments The arguments to be included as the value of the "Command" field.
     * @return A JSON string representing the request body, or an error JSON string
     * if an exception occurs during creation.
     */
    @Override
    public String createRequestBody(String arguments) {
        if (arguments == null || arguments.trim().isEmpty()) {
            throw new IllegalArgumentException("Arguments must not be null or empty.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"Command\":\"");
        sb.append(arguments);
        sb.append("\"}");
        return sb.toString();
    }
}