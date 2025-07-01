FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

# Install minimal network debugging tools
RUN apt-get update && \
    apt-get install -y dnsutils iputils-ping curl

# Set working directory for runtime stage
WORKDIR /app

# Copying the JAR file from the build stage
COPY --from=builder /app/target/worker-0.1.0.jar /app/worker.jar

# Copy the resources
COPY --from=builder /app/src/main/resources /app/src/main/resources

# Set the entry point to our debugging script
ENTRYPOINT ["java", "-jar", "/app/worker.jar"]
