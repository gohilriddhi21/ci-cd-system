package com.example.cliserver.backend.model;

import com.example.cliserver.backend.utils.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.cliserver.backend.utils.PipelineUtils.checkFieldIsString;


/**
 * Represents the configuration of a pipeline, including its stages and jobs.
 *
 * <p>This class encapsulates the pipeline details such as the pipeline name, stages, and jobs.
 */
public class PipelineConfig {
    private PipelineConfig() {
    }

    private Pipeline pipeline;

    /**
     * Gets the pipeline configuration.
     *
     * @return the {@link Pipeline} object representing the pipeline configuration
     */
    public Pipeline getPipeline() {
        return new Pipeline(pipeline);
    }

    /**
     * Represents a pipeline with its name, stages, and jobs.
     *
     * <p>This class validates the pipeline structure, ensuring that each stage and job is
     * well-defined and follows the required format.</p>
     */
    public static class Pipeline {
        private Pipeline() {
        }

        /**
         * Copy constructor for creating a new {@code Pipeline} object by copying the properties
         * from another existing {@code Pipeline}.
         *
         * @param copy the existing {@code Pipeline} object to copy from
         */
        public Pipeline(Pipeline copy) {
            this.name = copy.name;
            this.registry = copy.registry;
            this.image = copy.image;
            this.uploadRepo = copy.uploadRepo;
            this.stages = copy.stages;
            this.jobs = copy.jobs;
        }

        private String name;
        private String registry;
        private String image;
        private String uploadRepo;
        private List<String> stages;
        private List<Job> jobs;

        /**
         * Gets the name of the pipeline.
         *
         * @return the name of the pipeline
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the pipeline.
         *
         * <p>Validates that the name is of type String. Throws an {@link IllegalArgumentException}
         * if the value is not a string.</p>
         *
         * @param name the name of the pipeline
         */
        public void setName(Object name) {
            checkFieldIsString(name, "name");
            this.name = (String) name;
        }

        /**
         * Gets the docker registry for the pipeline.
         *
         * @return the registry for the pipeline
         */
        public String getRegistry() {
            return registry;
        }

        /**
         * Sets the registry for the pipeline.
         *
         * <p>Validates that the registry is of type String. Throws an
         * {@link IllegalArgumentException} if the value is not a string.</p>
         *
         * @param registry the registry of the pipeline
         */
        public void setRegistry(Object registry) {
            checkFieldIsString(registry, "registry");
            this.registry = (String) registry;
        }

        /**
         * Gets the docker image for the pipeline.
         *
         * @return the image for the pipeline
         */
        public String getImage() {
            return image;
        }

        /**
         * Sets the docker image for the pipeline.
         *
         * <p>Validates that the image is of type String. Throws an
         * {@link IllegalArgumentException} if the value is not a string.</p>
         *
         * @param image the image of the pipeline
         */
        public void setImage(Object image) {
            checkFieldIsString(image, "image");
            this.image = (String) image;
        }

        /**
         * Gets the upload repo path for the pipeline.
         *
         * @return the upload repo for the pipeline
         */
        public String getUploadRepo() {
            return uploadRepo;
        }

        /**
         * Sets the upload repo for the pipeline.
         *
         * <p>Validates that the uploadRepo is of type String. Throws an
         * {@link IllegalArgumentException} if the value is not a string.</p>
         *
         * @param uploadRepo the upload repo of the pipeline
         */
        public void setUploadRepo(Object uploadRepo) {
            checkFieldIsString(uploadRepo, "upload-repo");
            this.uploadRepo = (String) uploadRepo;
        }

        /**
         * Gets the list of stages in the pipeline.
         *
         * @return the list of stages in the pipeline
         */
        public List<String> getStages() {
            return stages == null ? Collections.emptyList() : new ArrayList<>(stages);
        }

        /**
         * Sets the stages of the pipeline.
         *
         * <p>Validates that the stages are not null and that each stage is of type String.
         * Throws an {@link IllegalArgumentException} if any stage is not a String.</p>
         *
         * @param stages the list of pipeline stages
         */
        public void setStages(List<?> stages) {
            if (stages == null) {
                this.stages = Constants.defaultPipelineStages;
                return;
            }
            // Check if every element in the list is a String
            for (Object item : stages) {
                checkFieldIsString(item, "stages");
            }
            this.stages = new ArrayList<String>((Collection<? extends String>) stages);
        }

        /**
         * Gets the list of jobs associated with the pipeline.
         *
         * @return the list of {@link Job} objects representing the jobs in the pipeline
         */
        public List<Job> getJobs() {
            return jobs == null ? Collections.emptyList() : new ArrayList<>(jobs);
        }

        /**
         * Sets the jobs for the pipeline.
         *
         * <p>Validates that the job names are unique and throws an {@link IllegalArgumentException}
         * if any job name is duplicated. Converts the ConfigJob objects from the YAML file into
         * Job objects.</p>
         *
         * @param jobs the list of jobs to set
         */
        public void setJobs(List<ConfigJob> jobs) {
            Set<String> jobNames = new HashSet<>();
            List<Job> convertedJobs = new ArrayList<>();

            for (ConfigJob job : jobs) {
                String jobName = job.getName();
                if (jobNames.contains(jobName)) {
                    throw new IllegalArgumentException("Job names must be unique. Name '"
                            + jobName + "' is duplicated.");
                }
                jobNames.add(jobName);
                convertedJobs.add(new Job(job));
            }
            this.jobs = convertedJobs;
        }
    }
}

