# Use OpenJDK 17 as base
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy Maven-built JAR
COPY target/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
