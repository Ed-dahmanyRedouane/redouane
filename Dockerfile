# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first (better caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the jar
RUN ./mvnw clean package -DskipTests


# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/target/bookshop-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]