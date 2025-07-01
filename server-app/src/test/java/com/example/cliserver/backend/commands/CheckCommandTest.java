package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.model.ValidationResult;
import com.example.cliserver.backend.model.PipelineConfig; // In case you need a non-null config.
import com.example.cliserver.backend.utils.ConfigurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CheckCommandTest {

    private CheckCommand checkCommand;
    private ConfigurationValidator mockValidator;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a new CheckCommand instance.
        checkCommand = new CheckCommand();
        // Create a mock instance of ConfigurationValidator using Mockito.
        mockValidator = mock(ConfigurationValidator.class);
        // Inject the mock into the CheckCommand instance.
        injectMockValidator(checkCommand, mockValidator);
    }

    private void injectMockValidator(CheckCommand command, ConfigurationValidator mockValidator) throws Exception {
        Field field = CheckCommand.class.getDeclaredField("configurationValidator");
        field.setAccessible(true);
        field.set(command, mockValidator);
    }

    @Test
    public void testValidateYamlValid() {
        String filename = "pipeline.yaml";
        String repo = "http://example.com/repo.git";
        String branch = "master";
        String commit = "abc123";
        ValidationResult validResult = new ValidationResult(true, (PipelineConfig) null);
        when(mockValidator.validateYaml(filename, repo, branch, commit)).thenReturn(validResult);

        String result = checkCommand.validateYaml(filename, repo, branch, commit);
        assertEquals("The file pipeline.yaml file is valid.", result);
        verify(mockValidator, times(1)).validateYaml(filename, repo, branch, commit);
    }

    @Test
    public void testValidateYamlInvalid() {
        String filename = "pipeline.yaml";
        String repo = "http://example.com/repo.git";
        String branch = "master";
        String commit = "abc123";

        String errorMessage = "Invalid YAML file structure.";
        ValidationResult invalidResult = new ValidationResult(false, errorMessage);
        when(mockValidator.validateYaml(filename, repo, branch, commit)).thenReturn(invalidResult);
        String result = checkCommand.validateYaml(filename, repo, branch, commit);
        assertEquals(errorMessage, result);
        verify(mockValidator, times(1)).validateYaml(filename, repo, branch, commit);
    }
}
