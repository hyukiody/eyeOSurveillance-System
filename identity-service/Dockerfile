FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./
COPY identity-service/pom.xml ./identity-service/

# Copy source code
COPY identity-service/src ./identity-service/src

# Build the service
RUN apk add --no-cache maven && \
    cd identity-service && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact
COPY --from=build /app/identity-service/target/*.jar app.jar

# Add health check dependencies
RUN apk add --no-cache wget

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
