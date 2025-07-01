# Tech Stack 📚

This document outlines the tools and technologies used in our **CI/CD system implementation**. Below is a structured breakdown of our tech stack, with links to official documentation and resources.

---

## Why This Stack? 🤔

This combination of tools and technologies ensures a **robust, scalable, and maintainable CI/CD pipeline**. From **code quality checks** to **automated deployments**, every aspect of the development lifecycle is covered.  
Additionally, we have used these technologies in our assignments, ensuring **team familiarity** with the code.

---

## Technology Stack Table 🏗️

### 1️⃣ Programming & Build Tools

| **Category**             | **Tool/Technology** | **Description**                                                                   | **Logo** | **Link** |
|--------------------------|---------------------|-----------------------------------------------------------------------------------|----------|----------|
| **Development IDE**      | IntelliJ IDEA       | A powerful IDE for Java development with debugging and productivity tools.        | ![IntelliJ IDEA](https://www.jetbrains.com/favicon.ico) | [IntelliJ IDEA](https://www.jetbrains.com/idea/) |
| **Programming Language** | Java                | The primary language for our CI/CD system, Java, is known for its performance and scalability. | ![Java](https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_language_logo.svg/121px-Java_programming_language_logo.svg.png) | [Java](https://www.java.com/) |
| **Build Tool**           | Maven               | Manages project dependencies and automates the build process.                     | ![Maven](https://maven.apache.org/favicon.ico) | [Maven](https://maven.apache.org/) |

---

### 2️⃣ Testing & Code Quality Tools

| **Category**           | **Tool/Technology** | **Description**                                                 | **Logo** | **Link** |
|------------------------|---------------------|-----------------------------------------------------------------|----------|----------|
| **Documentation Tool** | Javadoc             | Generates API documentation directly from Java source code.     | ![Javadoc](https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Java_programming_language_logo.svg/121px-Java_programming_language_logo.svg.png) | [Javadoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) |
| **Testing Framework**  | JUnit               | The go-to framework for writing and running unit tests in Java. | ![JUnit](https://junit.org/junit5/assets/img/junit5-logo.png) | [JUnit](https://junit.org/junit5/) |
| **Static Analysis**    | Checkstyle          | Ensures code adheres to a consistent style and format.          | ![Checkstyle](https://checkstyle.sourceforge.io/images/header-checkstyle-logo.png) | [Checkstyle](https://checkstyle.sourceforge.io/) |
| **Static Analysis**    | SpotBugs            | Detects potential bugs and problematic code patterns.           | ![SpotBugs](https://spotbugs.github.io/images/logos/spotbugs_icon_only_zoom_256px.png) | [SpotBugs](https://spotbugs.github.io/) |
| **Static Analysis**    | PMD                 | Analyzes code quality and identifies common programming flaws.  | ![PMD](https://pmd.github.io/favicon.ico) | [PMD](https://pmd.github.io/) |
| **Code Coverage**      | JaCoCo              | Measures test coverage and enforces minimum thresholds.         | ![JaCoCo](https://www.eclemma.org/jacoco/images/jacoco.png) | [JaCoCo](https://www.jacoco.org/jacoco/) |

---

### 3️⃣ Version Control & CI/CD

| **Category**        | **Tool/Technology** | **Description**                                                                  | **Logo** | **Link** |
|---------------------|---------------------|----------------------------------------------------------------------------------|----------|----------|
| **Version Control** | Git                 | An industry-standard version control system for tracking changes and collaboration. | ![Git](https://git-scm.com/favicon.ico) | [Git](https://git-scm.com/) |
| **CI/CD Platform**  | GitHub Actions      | Automates workflows, including builds, tests, and deployments.                   | ![GitHub Actions](https://github.com/favicon.ico) | [GitHub Actions](https://github.com/features/actions) |

---

### 4️⃣ Containerization & Deployment

| **Category**         | **Tool/Technology** | **Description**                                                  | **Logo** | **Link** |
|----------------------|---------------------|------------------------------------------------------------------|----------|----------|
| **Containerization** | Docker              | Simplifies deployment by packaging applications into containers. | ![Docker](https://www.docker.com/favicon.ico) | [Docker](https://www.docker.com/) |
| **Docker SDK**       | Docker Java         | Programmatic interaction with Docker in Java apps.               | ![Docker Java](https://avatars.githubusercontent.com/u/7772003?s=200&v=4) | [Docker Java](https://github.com/docker-java/docker-java) |

---

### 5️⃣ Java Libraries & Integrations

| **Category**             | **Library**            | **Description**                                                                     | **Logo** | **Link** |
|--------------------------|------------------------|-------------------------------------------------------------------------------------|----------|----------|
| REST API Framework       | Spring Boot            | Industry standard for Java REST APIs with built-in testing support.                 | ![Spring Boot](https://upload.wikimedia.org/wikipedia/commons/7/79/Spring_Boot.svg) | [Spring Boot](https://spring.io/projects/spring-boot) |
| Command-Line Parsing     | Apache Commons CLI     | Simple API for handling CLI arguments.                                              | ![Apache Commons CLI](https://commons.apache.org/proper/commons-cli/images/commons-logo.png) | [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) |
| Configuration Management | Jackson YAML           | Parses and handles YAML configurations.                                             | - | [Jackson YAML](https://github.com/FasterXML/jackson-dataformat-yaml) |
| Git & Version Control    | JGit                   | Java implementation of Git operations.                                              | ![JGit](https://git-scm.com/images/logo@2x.png) | [JGit](https://git-scm.com/book/pl/v2/Appendix-B:-Embedding-Git-in-your-Applications-JGit) |
| Database Management      | MongoDB                | Document-based NoSQL database, great for handling unstructured data.                | ![MongoDB](https://www.mongodb.com/favicon.ico) | [MongoDB](https://www.mongodb.com/) |
| Table Formatting         | AsciiTable             | Lightweight Java library for generating ASCII tables for reports.                   | - | [AsciiTable](https://github.com/vdmeer/asciitable) |
| Message Queue            | RabbitMQ               | Robust message broker for asynchronous processing and distributed systems.          | ![RabbitMQ](https://www.rabbitmq.com/img/rabbitmq-logo.svg) | [RabbitMQ](https://www.rabbitmq.com) |
| Object Storage Client    | MinIO                  | Java SDK to interact with object storage systems like S3 or MinIO.                  | ![MinIO](https://min.io/resources/img/logo.svg) | [MinIO](https://min.io/) |
| Data Format              | JSON                   | Lightweight data-interchange format for structured data representation.             | - | [JSON](https://github.com/stleary/JSON-java) |
| HTTP Client              | Apache HttpClient 5    | HTTP client used for making web requests.                                           | - | [Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.4.x/) |

---

## Conclusion 🎯

This **tech stack** was chosen based on **performance, maintainability, and team expertise**. It ensures a **scalable and efficient CI/CD pipeline**, empowering seamless software delivery. The additions from the provided `pom.xml` files, such as **JaCoCo**, **Docker Java**, **MinIO**, **Spring Boot**, and **Apache HttpClient 5** have been incorporated to reflect the actual implementation. This stack empowers our development and deployment process with modern DevOps practices and best-in-class Java tooling.
