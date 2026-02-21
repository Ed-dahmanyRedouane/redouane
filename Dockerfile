# ============================================================
# Stage 1 : Build with Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy POM first (Maven dependency cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and compile
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2 : Lightweight production image
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change file ownership
RUN chown spring:spring app.jar
USER spring

# Exposed port (must match server.port in application.yml)
EXPOSE 8080

# Default environment variable (overridden by docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Optimized entrypoint for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]