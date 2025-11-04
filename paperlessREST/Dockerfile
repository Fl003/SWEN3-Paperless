# syntax=docker/dockerfile:1

############################
# 1) build the app
############################
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -f pom.xml -pl paperlessREST -am -DskipTests clean package #build module rest and everything what it depends on

############################
# 2) runtime image
############################
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy the boot jar (name wildcard keeps it future-proof)
COPY --from=build /app/paperlessREST/target/*.jar app.jar

# optional JVM flags via JAVA_OPTS
ENV JAVA_OPTS=""

# non-root is nice-to-have
RUN useradd -ms /bin/bash appuser
USER appuser

EXPOSE 8081

# use a shell so $JAVA_OPTS is expanded (prevents “executable not found” error)
CMD ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
