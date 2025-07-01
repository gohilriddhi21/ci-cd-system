# Team 2 CI/CD System

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Usage](#usage)
4. [Development](#development)

## Introduction

This custom-made CI/CD system addresses needs not supported by existing systems. The system can be run remotely and locally by developers. A command line interface is provided for developers to use to run pipelines locally and to be used on server machines.

## Prerequisites

Before running the system, ensure you have the following installed:
- Java Development Kit (JDK) 11 or higher
- Maven
- Docker 

## Development

After cloning the project please run `mvn clean install` to get started.

### Running Tests

To run all the tests for this project run `mvn test`. 

### Generate Coverage Reports

To generate a code coverage report run `mvn jacoco:report`. A report will be generated at target/site/jacoco/edu/neu/cs6510/sp25/team2 .

### Code Documentation

To generate code documentation with Javadoc run `mvn javadoc:javadoc`. Javadoc documentation will be available at target/reports/apidocs/edu/neu/cs6510/sp25/team2 .

### Generate Code Style Report

To generate a code style report run `mvn checkstyle:check`. A report called `checkstyle-result.xml` will be generated in the target directory.

### Generate Static Analysis Report

To generate a static analysis report run `mvn spotbugs:check`. A report called `spotbugsXml.xml` will be generated in the target directory.

### Generate `jar` package

To generage a `jar` package run `mvn package`. The result will be in the target directory.

