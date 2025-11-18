FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN echo "====== FILES IN /app ======"
RUN ls -R /app

RUN echo "====== CONTENT OF /app/pom.xml ======"
RUN cat /app/pom.xml

#Build only ocr-worker
RUN mvn -pl ocr-worker -am -DskipTests clean package


# --- Runtime Image mit Tesseract ---
FROM eclipse-temurin:21-jre

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr tesseract-ocr-deu tesseract-ocr-eng \
    poppler-utils \
    ca-certificates fontconfig \
 && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

WORKDIR /app

RUN useradd -ms /bin/bash appuser
USER appuser

COPY --from=build /app/ocr-worker/target/*.jar app.jar

ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
