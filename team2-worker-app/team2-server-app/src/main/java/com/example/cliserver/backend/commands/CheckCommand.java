package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.model.ValidationResult;
import com.example.cliserver.backend.utils.ConfigurationValidator;

/**
 * Command to validate YAML pipeline configuration files.
 * Checks configuration files for syntax and structural correctness.
 */
public class CheckCommand {

    private final ConfigurationValidator configurationValidator;

    /**
     * Constructs a new CheckCommand with a default ConfigurationValidator.
     */
    public CheckCommand() {
        configurationValidator = new ConfigurationValidator();
    }


    /**
     * Validates a YAML pipeline configuration file and prints the result.
     * Outputs either success confirmation or the specific error message.
     *
     * @param filename The pipeline configuration file to validate
     * @param repo The repo url for the file to validate or null if local
     * @param branch The branch to check out for the given repo
     * @param commit The commit hash to check out for the given repo
     * @return A string message indicating whether the file is valid or a not
     */
    public String validateYaml(String filename, String repo, String branch, String commit) {
        ValidationResult validationResult =
                configurationValidator.validateYaml(filename, repo, branch, commit);
        if (validationResult.isValid()) {
            return "The file " + filename + " file is valid.";
        } else {
            return validationResult.getErrorMessage();
        }
    }
}
