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
# Create directory for uploads if it doesn't exist (configured in application.properties)
# Fix permissions so the 'spring' user can write to it
RUN mkdir -p uploads && chown spring:spring uploads

# Create entrypoint script inline to ensure correct line endings (LF)
RUN echo '#!/bin/sh' > entrypoint.sh && \
    echo 'if [ -n "$DB_URL" ]; then' >> entrypoint.sh && \
    echo '    echo "Configuring Database URL..."' >> entrypoint.sh && \
    echo '    # Render DB_URL format: postgres://user:pass@host:port/db' >> entrypoint.sh && \
    echo '    # Strip scheme' >> entrypoint.sh && \
    echo '    NO_SCHEME="${DB_URL#*://}"' >> entrypoint.sh && \
    echo '    # Extract user:pass' >> entrypoint.sh && \
    echo '    CREDS="${NO_SCHEME%@*}"' >> entrypoint.sh && \
    echo '    # Extract host:port/db' >> entrypoint.sh && \
    echo '    HOST_DB="${NO_SCHEME#*@}"' >> entrypoint.sh && \
    echo '    # Extract user and pass' >> entrypoint.sh && \
    echo '    DB_USER="${CREDS%:*}"' >> entrypoint.sh && \
    echo '    DB_PASS="${CREDS#*:}"' >> entrypoint.sh && \
    echo '    # Export standard Spring variables' >> entrypoint.sh && \
    echo '    export SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST_DB}"' >> entrypoint.sh && \
    echo '    export SPRING_DATASOURCE_USERNAME="${DB_USER}"' >> entrypoint.sh && \
    echo '    export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"' >> entrypoint.sh && \
    echo '    echo "Database configured with separate credentials."' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo 'exec "$@"' >> entrypoint.sh && \
    chmod +x entrypoint.sh

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "entrypoint.sh", "java", "-jar", "app.jar"]
