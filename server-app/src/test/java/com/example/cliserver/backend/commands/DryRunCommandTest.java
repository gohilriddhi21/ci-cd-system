package com.example.cliserver.backend.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineConfig;
import com.example.cliserver.backend.model.ValidationResult;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.PipelineUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

public class DryRunCommandTest {
    private final String filename = "test.yml";
    private final String repo = null;
    private final String branch = null;
    private final String commit = null;

    @Test
    public void testRunDry_withValidConfig_shouldReturnJobExecutionOrder() {
        PipelineConfig config = createMockPipelineConfig();
        try (MockedConstruction<ConfigurationValidator> mocked = mockConstruction(ConfigurationValidator.class,
            (mock, context) -> {
                ValidationResult result = mock(ValidationResult.class);
                when(result.isValid()).thenReturn(true);
                when(result.getConfig()).thenReturn(config);
                when(mock.validateYaml(filename, repo, branch, commit)).thenReturn(result);
            })) {

            Job job = new Job();
            job.setName("build");
            job.setStage("build-stage");
            config.getPipeline().setStages(List.of("build-stage"));
            config.getPipeline().setJobs(List.of(job));

            // Mock the static method
            try (MockedStatic<PipelineUtils> utilsMock = mockStatic(PipelineUtils.class)) {
                utilsMock.when(() -> PipelineUtils.getTopologicallySortedJobs(anyList()))
                    .thenReturn(List.of(job));

                String output = DryRunCommand.runDry(filename, repo, branch, commit);
                assertTrue(output.contains("build:"));
            }
        }
    }
    @Test
    public void testRunDry_withInvalidValidation_shouldReturnErrorMessage() {
        try (MockedConstruction<ConfigurationValidator> mocked = mockConstruction(ConfigurationValidator.class,
            (mock, context) -> {
                ValidationResult result = mock(ValidationResult.class);
                when(result.isValid()).thenReturn(false);
                when(result.getErrorMessage()).thenReturn("Invalid config");
                when(mock.validateYaml(filename, repo, branch, commit)).thenReturn(result);
            })) {

            String output = DryRunCommand.runDry(filename, repo, branch, commit);
            assertTrue(output.contains("Invalid config"));
        }
    }

    private PipelineConfig createMockPipelineConfig() {
        ConfigurationValidator validator = new ConfigurationValidator();
        ValidationResult validationResult = validator.validateYaml("../.pipelines/correct.yaml", null, null, null);
        return validationResult.getConfig();
    }

}
