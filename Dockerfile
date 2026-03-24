# Multi-stage build for Spring Boot 3.2 + JDK 21
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/skishop-monolith.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
