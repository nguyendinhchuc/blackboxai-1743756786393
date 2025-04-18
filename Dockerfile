# Use Maven to build the project
FROM maven:3.8.4-openjdk-21 AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Use OpenJDK runtime for the final image
FROM openjdk:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
