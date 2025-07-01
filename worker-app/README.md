# Team 2 CI/CD System

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Usage](#usage)
4. [Development](#development)

## Introduction

This custom made CI/CD system addresses needs not supported by existing systems. The system can be run remotely and locally by developers. A command line interface is provided for developers to use to run pipelines locally and to be used on server machines.

## Prerequisites

Before running the system, ensure you have the following installed:
- Java Development Kit (JDK) 11 or higher
- Maven
- MongoDB (local installation)
- RabbitMQ (for asynchronous execution)

## Usage

The CLI provides:

1. The option to specify the path to the configuration file, e.g., --filename | -f. The path given must be relative to the repo root folder. If a file is not given the CLI’s default is .pipeline/pipeline.yaml

2. A --check option that reads a pipeline configuration file, checks for the files validity and exits. This option is to check configuration files for any errors without running the pipeline.

3. A a --dry-run option that checks the validity of the configuration file and prints out the order of execution showing stages and their jobs.

4. A 'run' sub-command which given a valid pipeline configuration will execute the pipeline on the developer's local machine. By default, pipelines run asynchronously. When running with the verbose flag (--vv), pipelines execute synchronously.

5. A 'report' sub-command which returns pipeline run reports. The report command has options: --local to retrieve reports for the local repo,
    --repo to specify the repo to retrieve reports from and --pipeline to specify the pipeline name to retrieve reports for.

6. A 'status' sub-command which displays status information for pipeline runs, showing the current state of stages and jobs.

To run with the CLI only run 'mvn clean package'. Then use 'java -jar target/cicd-0.1.0.jar' followed by the commands and options as desired.

## Development

After cloning the project please run `mvn clean install` to get started.

### Running Tests

To run all the tests for this project run `mvn test`. 

### Generate Coverage Reports

To generate a code coverage report run `mvn jacoco:report`. A report will be generated at target/site/jacoco/com/example/cliserver .

### Code Documentation

To generate code documentation with Javadoc run `mvn javadoc:javadoc`. Javadoc documentation will be available at target/reports/apidocs/com.example/cliserver .

### Generate Code Style Report

To generate a code style report run `mvn checkstyle:check`. A report called `checkstyle-result.xml` will be generated in the target directory.

### Generate Static Analysis Report

To generate a static analysis report run `mvn spotbugs:check`. A report called `spotbugsXml.xml` will be generated in the target directory.

### Generate `jar` package

To generage a `jar` package run `mvn package`. The result will be in the target directory.