# Use a base image with Java 17 (or your version)
FROM openjdk:17-jdk-alpine

# Copy the jar into the container
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "/app.jar"]
