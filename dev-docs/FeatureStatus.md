# List of Completed Features

**Commands:**
![Screenshot 2025-04-14 at 8 12 06 pm](https://github.com/user-attachments/assets/a9b195ec-1142-42c5-b166-0801db2e2641)

## Use Cases

- U1. Remote Repo Local CI/CD Run
- U2. Local Repo Local CI/CD Run

## CICD Configuration File

- C1.Configuration files in a folder
- C2.Each configuration file is independent
- C3.There should be a global section in the configuration file
- C3.1.Jobs should be able to use or override global keys
- C3.2.Pipeline name is unique for the repository
- C4.Stages have a default but the configuration file can override
- C5.1.Job Configurations have a name
- C5.2.Job Configurations define their stage
- C5.3.Job Configurations define their Docker image
- C5.4.Job Configurations define 1 or more commands
- C5.5.Job Configurations can be set to allow failure
- C5.6.Job Configuration can have dependencies
- C5.6.1.Job dependencies cannot form cycles
- C5.7.Job configurations optionally can specify artifacts to upload
- C5.7.1.Specifying files and/or folders for upload
- C5.7.2.Artifact upload by default is performed if the job succeeds

## CLI

- L1.Check configuration file
- L2.Dry run option provides the execution order
- L3.Error reporting
- L4.Showing past pipeline runs
- L4.1.Show summary all past pipeline runs for a repository
- L4.2.Show pipeline run summary
- L4.3.Show stage summary
- L4.4.Show job summary
- L6.Running a pipeline

# List of Partly Implemented Features

## CLI

- L5.Local option
