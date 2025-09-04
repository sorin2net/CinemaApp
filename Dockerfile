FROM openjdk:23-jdk-slim

RUN apt-get update && apt-get install -y \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY src/ ./src/
COPY lib/ ./lib/
COPY data/ ./data/
COPY resources/ ./resources/

RUN mkdir -p out

RUN find src -name "*.java" > sources.txt && javac -d out -cp "lib/*" @sources.txt

ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"

CMD ["java", "-cp", "out:lib/*", "cinema.app.CinemaApp"]
