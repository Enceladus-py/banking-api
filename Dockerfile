# Use a lightweight Java 25 base image
FROM eclipse-temurin:25-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled jar file into the container
# (Make sure to run 'mvn clean package' before building this!)
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]