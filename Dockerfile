# --- Build stage: compile & package JAR ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -DskipTests dependency:go-offline

# compile & package
COPY src ./src
RUN mvn -X -DskipTests  clean package   
# <— THIS creates /app/target/*.jar

# --- Runtime stage: run JAR on a slim JRE ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR file
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]