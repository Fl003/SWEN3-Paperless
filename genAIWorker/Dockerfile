FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl genAIWorker -am -DskipTests clean package

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/genAIWorker/target/*.jar app.jar
ENV GEMINI_API_KEY=${GEMINI_API_KEY}

ENTRYPOINT ["java", "-jar", "app.jar"]
