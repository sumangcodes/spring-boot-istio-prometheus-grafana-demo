# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim
# Copy the jar file to the container
COPY target/service-b-0.0.1-SNAPSHOT.jar app.jar
# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]