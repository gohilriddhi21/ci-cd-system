# The HowTo Guide
This HowTo guide provides step-by-step instructions on setting up and using the system.


## Table of Contents

- [Introduction](#introduction)
- [Installation](#installation)
- [Features and Usage](#features-and-usage)
  - [Help and Usage Information](#help-and-usage-information)
  - [Check YAML File Configuration](#check-yaml-file-configuration)
  - [Dry Run Mode](#dry-run-mode)
  - [Run Pipelines](#run-pipelines)
  - [Verbose Logging](#verbose-logging)
  - [Generate Reports](#generate-reports)
  - [Check Pipeline Status](#check-pipeline-status)

## Introduction

This project is a **Command-Line Interface (CLI) tool for managing CI/CD pipelines**. It allows developers to define pipeline configurations in YAML, execute pipelines locally or remotely, validate configurations, and view summaries of past pipeline runs. The system supports both **local and remote repositories**, tracks job and stage-level execution, and provides detailed logs and reports.

## 📁 Repository Structure

Our main repo is [`t2-cicd`](https://github.com/CS6510-SEA-SP25/t2-cicd). Our entire cicd project is this one. Please use this repo. Our main does not have a pom.xml. But our packages, 
`team2-cli-app`, `team2-server-app`, and `team2-worker-app` have `pom.xml` and are built using the maven build tool. For building that you can do `mvn clean install` in those package folders. 

```
├── .github/workflows/            # GitHub Actions CI/CD workflows
├── .pipelines/                   # Pipeline configuration files
├── dependencies/                 # External or internal dependencies
├── dev-docs/                     # Design and documentation assets
├── 📦 team2-cli-app/             # CLI application module
├── 📦 team2-server-app/          # Backend server module
├── 📦 team2-worker-app/          # Worker service for job processing
├── .gitignore                    # Git ignore rules
├── FeatureStatus.md              # Feature tracking document
├── HowToDocument.md              # Developer guide or documentation
├── README.md                     # Project overview and setup guide
├── checkstyle.xml                # Code style configuration
├── docker-compose.override.yml   # Dev-specific Docker settings
├── docker-compose.yml            # Core infrastructure setup with Docker
├── pom.xml                       # Maven project configuration
```

## Installation

As a developer, you should take a clone, go inside the cloned repo, hit the `docker compose up --build` and then in `team-cli-app` run the commands. 
As a client, you could do docker pull image first, then do the same. 

Note: We have not pushed our image to docker hub yet. Once done, can pull the image from there and then build. 


**Prerequisites**
- Unix-based OS (MacOS/Linux)
- Java (Maven)
- [Docker](https://www.docker.com/) installed and running

In addition, we have used docker images for `MongoDB`, `MinIo`, `RabbitMQ`, and `docker-socket` to support TCP connections. 

[Please check the dokcer-compose.yaml file in this directory to know its configuration.](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/docker-compose.yml)
We also have a [Docker Override Compose File](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/docker-compose.override.yml). The purpose of this file is to provision and manage the core infrastructure services required by our CI/CD system using Docker. It provides a reproducible, isolated, and portable runtime environment for our images. 


## Install
Steps:
![Flow of commands](https://github.com/user-attachments/assets/6ee5c72b-8ebb-45ac-aa3c-f7b8ec62e71d)

### 1. Clone the repository
```bash
git clone git@github.com:CS6510-SEA-SP25/t2-cicd.git
cd t2-cicd
```
This repo has all the codes.
It has:
- CLI (`team2-cli-app`)
- CICD Application- Server (`team2-server-app`)
- Worker (`team2-worker-app`)

**NOTE:** `t2-cicd` parent directory does not have pom.xml. It has a docker compose file, which brings up the server and the worker. 


## RUN our Application
1. Start the Server
From `t2-cicd` 
```bash
docker compose up --build
docker compose up --build -d
```

Use -d for detached mode. 


2. Client
```bash
cd team2-cli-app

# Build Package
mvn clean install

# Start running commands
java -jar target/team2-cli.jar <commands>
```

## Features and Usage
**Help and Usage Information:** Display command-line options and descriptions.
```bash
java -jar target/team2-cli.jar --help
```

**Sample Output:**

<img width="683" alt="IMG_4030" src="https://github.com/user-attachments/assets/9ddd788e-8476-461d-9b00-c17b22bdc2ef" />

**File Command** Check if the pipeline YAML file is present or not.
```bash
java -jar target/team2-cli.jar -f --check -f <yaml-pipeline-filename>

java -jar target/team2-cli.jar --filename -f correct.yaml
```

**Sample Output:**

<img width="1065" alt="IMG_1999" src="https://github.com/user-attachments/assets/23486084-bd97-4f8f-93c7-213a4d2e43c2" />


**Check YAML File Configuration:** Check if the pipeline YAML file is correct.
Check pipeline configuration files for correctness.
The pipeline YAML files must have the correct:
- syntax
- stages
- jobs
- dependencies. (no circular dependency allowed)

```bash
java -jar target/team2-cli.jar --check -f <yaml-pipeline-filename>

java -jar target/team2-cli.jar --check -f correct.yaml
```

**Sample Output:**

<img width="1065" alt="IMG_1999" src="https://github.com/user-attachments/assets/b7d4a7d1-273e-49ef-ae53-a3ee8067448b" />


**Dry Run Mode:** Checks the validity of the configuration file and prints out the order of execution showing stages and their jobs. It checks the validity of the configuration file and prints out the order of execution, showing stages and their jobs.
```bash
java -jar target/team2-cli.jar --dry-run -f <yaml-pipeline-filename>

java -jar target/team2-cli.jar --dry-run -f correct.yaml
```

**Sample Output:**

<img width="1337" alt="IMG_9971" src="https://github.com/user-attachments/assets/41bba837-081e-42b1-bdc4-042abf726519" />


**Run Pipelines:** Execute pipelines locally or from a specified repository. The system will return a unique runNumber that you can use to check pipeline status.A valid YAML file must be present in `/.pipelines/`
```bash
java -jar target/team2-cli.jar run -f <yaml-pipeline-filename>

java -jar target/team2-cli.jar run -f correct.yaml
```

**Sample Output:**

<img width="795" alt="IMG_4926" src="https://github.com/user-attachments/assets/b8f9a9f3-ecbd-47b8-a9b5-c2f7c26a2a6f" />


**Verbose Logging:** Enable detailed logs during pipeline execution.
```bash
java -jar target/team2-cli.jar run -f <yaml-pipeline-filename> --vv

java -jar target/team2-cli.jar run -f correct.yaml --vv
```

**Sample Output:**
![Screenshot 2025-04-15 at 8 36 30 am](https://github.com/user-attachments/assets/5d0f600f-4e39-4e2a-b53b-ad9cf4094f5f)


**Generate Reports:** Retrieve and display reports for specific pipeline runs.
```bash
java -jar target/team2-cli.jar report <repo-name> --pipeline <pipeline-name> --stage <stage-name>
java -jar target/team2-cli.jar report <repo-name> --pipeline <pipeline-name> --stage <stage-name> --job <job-name>

# Table format
java -jar target/team2-cli.jar report <repo-name> --pipeline <pipeline-name> --format table
java -jar target/team2-cli.jar report <repo-name> --pipeline <pipeline-name> --stage <stage-name> --job <job-name> --format table


Examples:

java -jar target/team2-cli.jar report --local --pipeline riddhi-pipeline --stage build-not-applicable
java -jar target/team2-cli.jar report --repo <repo-path>  --pipeline riddhi-pipeline --stage build 

java -jar target/team2-cli.jar report --local --pipeline riddhi-pipeline --stage build-not-applicable --job build-job
java -jar target/team2-cli.jar report --repo <repo-path> --pipeline riddhi-pipeline --stage build-not-applicable --job build-job
```

**Sample Output:**
***Stage and Job Report***

<img width="1358" alt="IMG_3254" src="https://github.com/user-attachments/assets/ab76ad53-bd7d-471e-99e5-068a36aadf20" />

***Stage Report without Jobs:***

<img width="1347" alt="IMG_7478" src="https://github.com/user-attachments/assets/2d8dba3a-b18f-4043-b43d-3507bbedfdab" />


**Check Pipeline Status:** Execute pipelines locally or from a specified repository. The system will return a unique run number that you can use to check the pipeline status. A valid YAML file must be present in `/.pipelines/`
```bash
java -jar target/team2-cli.jar status -f <yaml-pipeline-filename> --runNumber <your-run-number>

java -jar target/team2-cli.jar status -f <yaml-pipeline-filename> 
```

**Sample Output:**

<img width="834" alt="IMG_8832" src="https://github.com/user-attachments/assets/a5010689-251c-468e-a712-d9019fe408ab" />



**Repo Command**: Takes the git clone and runs the pipeline. 
```bash
java -jar target/team2-cli.jar run --repo <public-repo-link> -f <pipeline-name> -c | run | ...
```

**Sample Output:**

<img width="873" alt="IMG_3593" src="https://github.com/user-attachments/assets/32c626cd-1708-46db-9ec3-425b7b78bd89" />



# Files
1. [FeatureStatus.md](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/FeatureStatus.md)
2. [System Design](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/dev-docs/design/components.png)
3. [CLI Requirements Implemented](https://github.com/user-attachments/assets/9b35ae2c-e9dd-4d37-b8f0-c3d4c41bf4a4)
4. [Data Store: MongoDB](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/database_design.md)
6. [API Overview](https://github.com/CS6510-SEA-SP25/t2-cicd/blob/main/api_overview.md)

---

