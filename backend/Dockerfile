# Multi-stage build for Syntopia Backend
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:21-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r syntopia && useradd -r -g syntopia syntopia

# Copy JAR from build stage
COPY --from=build /app/target/syntopia-backend-*.jar app.jar

# Create logs directory
RUN mkdir -p /logs && chown -R syntopia:syntopia /logs /app

USER syntopia

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
