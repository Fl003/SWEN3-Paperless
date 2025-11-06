FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
#Build only ocr-worker
RUN mvn -pl ocr-worker -am -DskipTests clean package

#----runtime----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -ms /bin/bash appuser
USER appuser
COPY --from=build /app/ocr-worker/target/*.jar app.jar

ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
