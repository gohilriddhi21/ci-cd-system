FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Install Maven
RUN apt-get update &&  \
    apt-get install -y maven

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

# Set working directory for runtime stage
WORKDIR /app

# Copying the JAR file from the build stage
COPY --from=builder /app/target/Cli-Server-0.0.1-SNAPSHOT.jar /app/server.jar

# Copy the resources/
COPY --from=builder /app/src/main/resources /app/src/main/resources

EXPOSE 8080

# Set the entry point for the application
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
