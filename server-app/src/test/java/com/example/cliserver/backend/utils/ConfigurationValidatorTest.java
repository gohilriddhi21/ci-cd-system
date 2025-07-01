package com.example.cliserver.backend.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.cliserver.backend.model.ValidationResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ConfigurationValidatorTest {

    private ConfigurationValidator validator;

    @TempDir
    Path tempDir;

    private Path pipelineFile;
    private Path directoryPath;

    @BeforeEach
    public void setUp() throws IOException {
        validator = new ConfigurationValidator();
        directoryPath = tempDir.resolve("pipelines");
        Files.createDirectories(directoryPath);
    }

    @Test
    public void testValidateYaml_LocalValidFile_Success() throws Exception {
        String validYaml = createValidYaml();
        pipelineFile = tempDir.resolve("valid-pipeline.yaml");
        Files.write(pipelineFile, validYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertTrue(result.isValid());
        assertNotNull(result.getConfig());
        assertEquals("test-pipeline", result.getConfig().getPipeline().getName());
    }

    @Test
    public void testValidateYaml_MissingPipeline_ReturnsError() throws Exception {
        String invalidYaml = "name: test-pipeline\njobs: []";
        pipelineFile = tempDir.resolve("missing-pipeline.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Error"));
    }

    @Test
    public void testValidateYaml_MissingName_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  stages: [build, test]\n  jobs:\n    - name: job1\n      stage: build\n      script: [echo hello]";
        pipelineFile = tempDir.resolve("missing-name.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("missing key: name"));
    }

    @Test
    public void testValidateYaml_MissingJobs_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build, test]";
        pipelineFile = tempDir.resolve("missing-jobs.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("missing key: jobs"));
    }

    @Test
    public void testValidateYaml_EmptyJobs_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build, test]\n  jobs: []";
        pipelineFile = tempDir.resolve("empty-jobs.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("syntax error missing key: jobs"));
    }

    @Test
    public void testValidateYaml_JobMissingFields_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build]\n  jobs:\n    - name: job1\n      stage: build";
        pipelineFile = tempDir.resolve("job-missing-fields.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("missing key for job 'job1': script"));
    }

    @Test
    public void testValidateYaml_UndefinedStage_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build, test]\n  jobs:\n    - name: job1\n      stage: deploy\n      script: [echo hello]";
        pipelineFile = tempDir.resolve("undefined-stage.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("undefined stage"));
    }

    @Test
    public void testValidateYaml_DependencyInLaterStage_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build, test]\n  jobs:\n    - name: job1\n      stage: test\n      script: [echo hello]\n    - name: job2\n      stage: build\n      script: [echo world]\n      needs: [job1]";
        pipelineFile = tempDir.resolve("dependency-in-later-stage.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("depends on"));
        assertTrue(result.getErrorMessage().contains("which runs in a later stage"));
    }

    @Test
    public void testValidateYaml_CyclicDependency_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n  name: test-pipeline\n  stages: [build]\n  jobs:\n    - name: job1\n      stage: build\n      script: [echo hello]\n      needs: [job2]\n    - name: job2\n      stage: build\n      script: [echo world]\n      needs: [job1]";
        pipelineFile = tempDir.resolve("cyclic-dependency.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Cycle detected in jobs"));
    }

    @Test
    public void testValidateYaml_InvalidSyntax_ReturnsError() throws Exception {
        String invalidYaml = "::: not a valid yaml :::";
        pipelineFile = tempDir.resolve("invalid-syntax.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testValidateYaml_NonExistentFile_ReturnsError() {
        String nonexistentFile = tempDir.resolve("nonexistent.yaml").toString();
        ValidationResult result = validator.validateYaml(nonexistentFile, Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Error"));
    }

    @Test
    public void testValidateYaml_RemoteRepo_Success() throws Exception {
        if (!canConnectToGit()) {
            return;
        }
        String repo = "https://github.com/some-user/some-repo.git";
        String branch = "main";
        String commit = "hash-key";
        String filename = "pipeline.yaml";
        Path localRepoDir = tempDir.resolve("repo");
        Files.createDirectories(localRepoDir);
        Path localFile = localRepoDir.resolve(filename);
        Files.write(localFile, createValidYaml().getBytes());

        try {
            Method getRepoDirectoryFromURLMethod = getAccessibleMethod(GitUtils.class, "getRepoDirectoryFromURL", String.class);
            getRepoDirectoryFromURLMethod.invoke(null, repo);
            validator = new CustomTestValidator(localRepoDir.toString() + File.separator);
            ValidationResult result = validator.validateYaml(filename, repo, branch, commit);
            assertTrue(result.isValid());
            assertNotNull(result.getConfig());
        } catch (InvocationTargetException | IllegalAccessException e) {
            System.out.println("Skipping remote repo test: " + e.getMessage());
        }
    }

    @Test
    public void testValidateYaml_EmptyStages_UsesDefaultStages() throws Exception {
        // Arrange
        String yamlWithEmptyStages = "pipeline:\n" +
            "  name: empty-stages-pipeline\n" +
            "  stages: []\n" +
            "  jobs:\n" +
            "    - name: build-job\n" +
            "      stage: build\n" +
            "      script: [echo \"Building...\"]\n" +
            "    - name: test-job\n" +
            "      stage: test\n" +
            "      script: [echo \"Testing...\"]\n" +
            "    - name: doc-job\n" +
            "      stage: doc\n" +
            "      script: [echo \"Documenting...\"]\n" +
            "    - name: deploy-job\n" +
            "      stage: deploy\n" +
            "      script: [echo \"Deploying...\"]";
        pipelineFile = tempDir.resolve("empty-stages.yaml");
        Files.write(pipelineFile, yamlWithEmptyStages.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertTrue(result.isValid(), "Pipeline with empty stages should be valid when jobs match default stages");
        assertNotNull(result.getConfig());
    }

    @Test
    public void testValidateYaml_DependencyNotExist_ReturnsError() throws Exception {
        String invalidYaml = "pipeline:\n" +
            "  name: test-pipeline\n" +
            "  stages: [build, test]\n" +
            "  jobs:\n" +
            "    - name: job1\n" +
            "      stage: build\n" +
            "      script: [echo hello]\n" +
            "    - name: job2\n" +
            "      stage: test\n" +
            "      script: [echo world]\n" +
            "      needs: [nonexistent-job]";
        pipelineFile = tempDir.resolve("dependency-not-exist.yaml");
        Files.write(pipelineFile, invalidYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Dependency 'nonexistent-job' for job 'job2' does not exist."));
    }

    @Test
    public void testValidateYaml_UnknownProperty_DoesNotCauseFailure() throws Exception {
        String validYamlWithUnknown = "unknownKey: someValue\n" + createValidYaml();
        pipelineFile = tempDir.resolve("unknown-property.yaml");
        Files.write(pipelineFile, validYamlWithUnknown.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertTrue(result.isValid());
        assertNotNull(result.getConfig());
    }

    @Test
    public void testValidateYaml_ValidYamlWithExtraWhitespace_Success() throws Exception {
        String validYaml = "\n\n" + createValidYaml() + "\n\n";
        pipelineFile = tempDir.resolve("whitespace.yaml");
        Files.write(pipelineFile, validYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertTrue(result.isValid());
        assertNotNull(result.getConfig());
    }

    @Test
    public void testValidateYaml_EmptyFile_ReturnsError() throws Exception {
        String emptyYaml = "";
        pipelineFile = tempDir.resolve("empty.yaml");
        Files.write(pipelineFile, emptyYaml.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Error"));
    }

    @Test
    public void testValidateYaml_WhitespaceOnlyFile_ReturnsError() throws Exception {
        String whitespaceOnly = "   ";
        pipelineFile = tempDir.resolve("whitespace-only.yaml");
        Files.write(pipelineFile, whitespaceOnly.getBytes());
        ValidationResult result = validator.validateYaml(pipelineFile.toString(), Constants.LOCAL_REPO, null, null);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Error"));
    }

    ///////////////// Helper Methods //////////////////

    private Method getAccessibleMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private boolean canConnectToGit() {
        try {
            Process process = Runtime.getRuntime().exec("git --version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String createValidYaml() {
        return "pipeline:\n" +
            "  name: test-pipeline\n" +
            "  stages: [build, test, deploy]\n" +
            "  jobs:\n" +
            "    - name: build-job\n" +
            "      stage: build\n" +
            "      script: [echo \"Building...\"]\n" +
            "    - name: test-job\n" +
            "      stage: test\n" +
            "      script: [echo \"Testing...\"]\n" +
            "      needs: [build-job]\n" +
            "    - name: deploy-job\n" +
            "      stage: deploy\n" +
            "      script: [echo \"Deploying...\"]\n" +
            "      needs: [test-job]";
    }

    private class CustomTestValidator extends ConfigurationValidator {
        private final String localDirectory;

        public CustomTestValidator(String localDirectory) {
            this.localDirectory = localDirectory;
        }

        @Override
        public ValidationResult validateYaml(String filename, String repo, String branch, String commit) {
            if (repo != null && !repo.isEmpty() && !repo.equals(Constants.LOCAL_REPO)) {
                return super.validateYaml(localDirectory + filename, Constants.LOCAL_REPO, null, null);
            }
            return super.validateYaml(filename, repo, branch, commit);
        }
    }
}
