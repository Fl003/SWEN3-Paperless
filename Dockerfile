# syntax=docker/dockerfile:1

############################
# 1) build the app
############################
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

############################
# 2) runtime image
############################
FROM eclipse-temurin:21-jre
WORKDIR /app
# non-root is nice-to-have
RUN useradd -ms /bin/bash appuser
USER appuser

# copy the boot jar (name wildcard keeps it future-proof)
COPY --from=build /app/target/*SNAPSHOT.jar app.jar

EXPOSE 8081
# optional JVM flags via JAVA_OPTS
ENV JAVA_OPTS=""

# start Spring Boot
ENTRYPOINT ["java $JAVA_OPTS -jar app.jar"]
