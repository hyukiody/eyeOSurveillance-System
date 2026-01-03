FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./
COPY stream-processing/pom.xml ./stream-processing/

# Copy source code
COPY stream-processing/src ./stream-processing/src

# Build the service
RUN apk add --no-cache maven && \
    cd stream-processing && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact
COPY --from=build /app/stream-processing/target/*.jar app.jar

# Add health check dependencies
RUN apk add --no-cache wget

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
