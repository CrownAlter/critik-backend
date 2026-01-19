# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create directory for uploads if it doesn't exist (configured in application.properties)
RUN mkdir -p uploads

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY entrypoint.sh .
# root owns it by default, but we can make it executable before switching user or change ownership
# Note: COPY preserves permissions in some cases, but best to chmod. 
# But we are already USER spring. We should do this before switching USER.

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh", "java", "-jar", "app.jar"]
