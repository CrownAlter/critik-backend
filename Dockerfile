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

# Create entrypoint script inline to ensure correct line endings (LF)
RUN echo '#!/bin/sh' > entrypoint.sh && \
    echo 'if [ -n "$DB_URL" ]; then' >> entrypoint.sh && \
    echo '    echo "Configuring Database URL..."' >> entrypoint.sh && \
    echo '    # Convert postgres:// to jdbc:postgresql://' >> entrypoint.sh && \
    echo '    CLEAN_URL=$(echo "$DB_URL" | sed -e "s/^postgres:/jdbc:postgresql:/" -e "s/^postgresql:/jdbc:postgresql:/")' >> entrypoint.sh && \
    echo '    export SPRING_DATASOURCE_URL="$CLEAN_URL"' >> entrypoint.sh && \
    echo '    echo "Database URL configured."' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo 'exec "$@"' >> entrypoint.sh && \
    chmod +x entrypoint.sh

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "entrypoint.sh", "java", "-jar", "app.jar"]
