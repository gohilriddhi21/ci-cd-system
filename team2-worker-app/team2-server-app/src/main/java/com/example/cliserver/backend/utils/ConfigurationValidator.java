package com.example.cliserver.backend.utils;


import static com.example.cliserver.backend.utils.PipelineUtils.hasCyclicDependency;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineConfig;
import com.example.cliserver.backend.model.ValidationResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class for validating YAML pipeline configuration files.
 *
 * <p>The {@link ConfigurationValidator} class provides methods to validate a YAML pipeline
 * configuration file by deserializing it into a {@link PipelineConfig} object and performing
 * various checks to ensure the validity of the file's structure and content. The validation checks
 * include verifying required fields, ensuring that stages have corresponding jobs, and detecting
 * any cyclical dependencies in job definitions.</p>
 *
 * <p>The main validation method is {@link #validateYaml(String, String, String, String)}, which
 * processes the YAML file and checks for errors in the configuration.
 *
 * <p>This class is designed to be used for validating pipeline configuration files before further
 * processing.</p>
 */
public class ConfigurationValidator {
    /**
     * Constructs a new ConfigurationValidator instance.
     */
    public ConfigurationValidator() {
    }

    /**
     * Validates the syntax and structure of a YAML pipeline configuration file.
     *
     * <p>This method reads the provided YAML file, deserializes it into
     * a {@link PipelineConfig} object, and performs validation checks, including:
     * - Verifying required fields like pipeline name, stages, and jobs.
     * - Ensuring each stage has associated jobs.
     * - Checking for undefined dependencies and cyclic job dependencies.
     *
     * <p>If validation passes, it returns a {@link ValidationResult} with the isValid boolean set
     * to true. Otherwise, isValid is false and the ValidationResult also contains the error message
     * error message indicating the issue.</p>
     *
     * @param filename the path to the YAML configuration file to validate
     * @param repo the url of the remote repo where the file to validate is, or null if the file is
     *             local
     * @param branch The branch to checkout for the given repo
     * @param commit The commit hash to checkout for the given repo
     * @return a {@link ValidationResult}.
     * @throws IllegalArgumentException if validation fails due to missing fields,
     *                                  invalid stages, or cyclic dependencies.
     */
    public ValidationResult validateYaml(String filename, String repo, String branch,
                                         String commit) {
        ValidationResult result;
        if (isRemoteRepo(repo)) {
            String localDir = GitUtils.getRepoDirectoryFromURL(repo);
            GitUtils.cloneRemoteRepo(localDir, repo, branch, commit);
            String directoryName = localDir + Constants.DIRECTORY;

            System.out.println("\n\n");
            System.out.println("Repo: " + repo);
            System.out.println("localDir: " + localDir);
            System.out.println("directoryName: " + directoryName);
            System.out.println("localDirName + filename, dir: " + localDir
                    + filename + ", " + directoryName);
            System.out.println("\n\n");

            result = validateYamlInDirectory(localDir + filename,
                    directoryName);
            GitUtils.cleanUpRemoteDirectory(repo);
            System.out.println("\n\n");
        } else {
            result = validateYamlInDirectory(filename, Constants.DIRECTORY);
        }
        return result;
    }

    /**
     * Helper method to validate the yaml file in the given directory
     *
     * @param filename the file to validate
     * @param directoryName the directory where the file will be stored
     * @return the {@link ValidationResult} for this file
     */
    private ValidationResult validateYamlInDirectory(String filename, String directoryName) {
        PipelineConfig pipelineConfig;
        try {
            pipelineConfig = loadPipeline(filename);
            if (pipelineConfig == null) {
                String errorMessage = filename + "Failed to load pipeline configuration.";
                return new ValidationResult(false, errorMessage);
            }
            checkRequiredField(pipelineConfig.getPipeline(), "missing key: pipeline");
            PipelineConfig.Pipeline pipeline = pipelineConfig.getPipeline();
            checkRequiredField(pipeline.getName(), "missing key: name");
            checkPipelineNameIsUnique(pipeline.getName(), directoryName);
            checkRequiredField(pipeline.getJobs(), "missing key: jobs");
            validateStagesAndJobs(pipeline);
            for (Job job : pipeline.getJobs()) {
                validateJob(job);
            }
            Set<String> jobCycle = hasCyclicDependency(pipeline.getJobs());
            if (!jobCycle.isEmpty()) {
                throw new IllegalArgumentException("Cycle detected in jobs: " + jobCycle);
            }
        } catch (IllegalArgumentException e) {
            String errorMessage = filename + ": syntax error " + e.getMessage();
            return new ValidationResult(false, errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error in file " + filename + ": " + e.getMessage();
            return new ValidationResult(false, errorMessage);
        }
        return new ValidationResult(true, pipelineConfig);
    }

    /**
     * Returns whether a remote repo has been specified by checked the value passed for repo
     * @param repo the repo value passed
     * @return true if the (remote) repo has been set else false
     */
    private boolean isRemoteRepo(String repo) {
        return repo != null && !repo.isEmpty() && !repo.equals(Constants.LOCAL_REPO);
    }


    /**
     * Validates that a job has all required fields: name, stage, image, and script.
     *
     * @param job The job to validate.
     * @throws IllegalArgumentException if any required field is missing.
     */
    private void validateJob(Job job) {
        checkRequiredField(job.getName(), "missing key for job '" + job.getName()
                + "': name");
        checkRequiredField(job.getStage(), "missing key for job '" + job.getName()
                + "': stage");
        checkRequiredField(job.getScript(), "missing key for job '" + job.getName()
                + "': script");
    }

    /**
     * Validates the stages, jobs, and dependencies of the pipeline.
     *
     * <p>This method checks if each job has a defined stage, ensures that each stage
     * has at least one job, and verifies that all job dependencies exist. If any of
     * these conditions are not met, an {@link IllegalArgumentException} is thrown
     * with a message indicating the specific issue.</p>
     *
     * @param pipeline the {@link PipelineConfig.Pipeline} object containing the stages
     *                 and jobs to validate
     * @throws IllegalArgumentException if a stage has no jobs, a job references an
     *                                  undefined stage, or a dependency references a non-existent
     *                                  job
     */
    private void validateStagesAndJobs(PipelineConfig.Pipeline pipeline) {
        // If stages key was missing entirely - default stages still need to be set
        if (pipeline.getStages().isEmpty()) {
            pipeline.setStages(Constants.defaultPipelineStages);
        }
        Set<String> definedStages = new HashSet<>(pipeline.getStages());

        for (Job job : pipeline.getJobs()) {
            if (!definedStages.contains(job.getStage())) {
                throw new IllegalArgumentException("Job '" + job.getName() + "' specifies " +
                        "undefined stage '" + job.getStage() + "'.");
            }
        }

        for (String stage : pipeline.getStages()) {
            if (pipeline.getJobs().stream().noneMatch(job -> job.getStage().equals(stage))) {
                throw new IllegalArgumentException("No jobs defined for stage '" + stage + "'");
            }
        }

        // Validate that each job's dependencies exist
        for (Job job : pipeline.getJobs()) {
            if (job.getNeeds() != null) {
                for (String dependency : job.getNeeds()) {
                    // Ensure the dependency exists
                    Job dependencyJob = pipeline.getJobs().stream()
                            .filter(j -> j.getName().equals(dependency))
                            .findFirst()
                            .orElse(null);

                    if (dependencyJob == null) {
                        throw new IllegalArgumentException(
                                String.format("Dependency '%s' for job '%s' does not exist.",
                                        dependency, job.getName())
                        );
                    }

                    // Ensure dependency jobs appear in earlier stages (cross-stage allowed if it's
                    // sequential)
                    int jobStageIndex = pipeline.getStages().indexOf(job.getStage());
                    int depStageIndex = pipeline.getStages().indexOf(dependencyJob.getStage());

                    if (depStageIndex > jobStageIndex) {
                        throw new IllegalArgumentException(
                                String.format("Job '%s' depends on '%s' which runs in a later " +
                                        "stage.", job.getName(), dependency)
                        );
                    }
                }
            }
        }

    }

    /**
     * Checks if a field is non-null and non-empty (if it's a list).
     *
     * @param fieldValue The field to validate.
     * @param message    Error message if validation fails.
     * @throws IllegalArgumentException if null or empty.
     */
    private void checkRequiredField(Object fieldValue, String message) {
        if (fieldValue == null || (fieldValue instanceof List &&
                ((List<?>) fieldValue).isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Loads and parses the pipeline configuration from a YAML file.
     *
     * <p>This method reads the specified YAML file and deserializes it into
     * a {@link PipelineConfig} object. If the file cannot be read, or if the
     * YAML content is invalid, it throws a {@link RuntimeException} with a
     * detailed error message indicating the file, line, and column where the
     * error occurred.</p>
     *
     * @param filename the path to the YAML file containing the pipeline configuration
     * @return a {@link PipelineConfig} object representing the parsed pipeline configuration
     * @throws RuntimeException if the file cannot be read or the YAML content is invalid
     */
    private PipelineConfig loadPipeline(String filename) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            File yamlFile = new File(filename);
            return mapper.readValue(yamlFile, PipelineConfig.class);
        } catch (JsonMappingException e) {
            if (e.getLocation() != null) {
                throw new RuntimeException(String.format("%s:%d:%d: %s",
                        filename,
                        e.getLocation().getLineNr(),
                        e.getLocation().getColumnNr(),
                        e.getOriginalMessage()));
            } else {
                throw new RuntimeException("Error: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading YAML file " + filename + ": " + e.getMessage()
            );
        }
    }

    private void checkPipelineNameIsUnique(String pipelineNameToCheck, String directory) {
        File dir = new File(directory);
        // Map to store pipeline names as keys and list of filenames as values
        Map<String, List<String>> pipelineNameMap = new HashMap<>();
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".yml") ||
                name.endsWith(".yaml"));
        if (files == null) {
            return;
        }
        YAMLMapper yamlMapper = new YAMLMapper();
        for (File file : files) {
            try {
                JsonNode rootNode = yamlMapper.readTree(file);

                JsonNode pipelineNode = rootNode.path("pipeline");
                JsonNode nameNode = pipelineNode.path("name");
                if (nameNode != null) {
                    String pipelineName = nameNode.asText();
                    pipelineNameMap.putIfAbsent(pipelineName, new ArrayList<>());
                    pipelineNameMap.get(pipelineName).add(file.getName());
                }
            } catch (IOException e) {
                System.out.println("Error reading yaml file: " + file.getName());
            }
        }
        List<String> filesWithPipelineName = pipelineNameMap.get(pipelineNameToCheck);
        if (filesWithPipelineName.size() > 1) {
            StringBuilder errorMessage = new StringBuilder();
            for (String file : filesWithPipelineName) {
                errorMessage.append("  - ")
                        .append(file)
                        .append("\n");
            }
            errorMessage.append("\n");
            throw new IllegalArgumentException("pipeline name: " + pipelineNameToCheck + " is not "
                    + "unique. Found the same pipeline name in files:\n" + errorMessage.toString());
        }
    }
}