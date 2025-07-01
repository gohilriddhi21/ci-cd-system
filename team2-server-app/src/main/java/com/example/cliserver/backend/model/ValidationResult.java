package com.example.cliserver.backend.model;

/**
 * Represents the result of a validation check.
 * This class contains information on whether the validation was successful
 * and, if not, an associated error message.
 */
public class ValidationResult {
    private final boolean isValid;
    private String errorMessage;

    private PipelineConfig config;

    /**
     * Constructs a ValidationResult with the specified validity.
     * The error message will be null if validation is successful.
     *
     * @param isValid A boolean indicating whether the validation was successful.
     * @param config the PipelineConfig representation of the validated pipeline
     */
    public ValidationResult(boolean isValid, PipelineConfig config) {
        this.isValid = isValid;
        this.config = config;
    }

    /**
     * Constructs a ValidationResult with the specified validity and error message.
     *
     * @param isValid      A boolean indicating whether the validation was successful.
     * @param errorMessage The error message associated with the validation failure.
     */
    public ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns whether the validation was successful.
     *
     * @return {@code true} if the validation is successful, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Returns the error message associated with a failed validation.
     * If validation is successful, this may return null.
     *
     * @return The error message, or {@code null} if validation is successful.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Gets the PipelineConfig
     * @return Pipeline config for the file that was validated
     */
    public PipelineConfig getConfig() {
        return this.config;
    }
}
