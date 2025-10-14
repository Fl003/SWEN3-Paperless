FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -ms /bin/bash appuser
USER appuser
COPY --from=build /app/target/*.jar app.jar
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENTRYPOINT ["java", "-jar", "app.jar"]
