# 1️⃣ Folosim un JDK slim pentru performanță
FROM openjdk:23-jdk-slim

# 2️⃣ Instalăm librăriile necesare (headless + font)
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

# 3️⃣ Setăm directorul de lucru în container
WORKDIR /app

# 4️⃣ Copiem sursele și librăriile
COPY src/ ./src/
COPY lib/ ./lib/
COPY data/ ./data/
COPY resources/ ./resources/

# 5️⃣ Creăm folderul pentru compilare
RUN mkdir -p out

# 6️⃣ Compilăm toate fișierele Java, incluzând librăriile din lib/
RUN find src -name "*.java" > sources.txt && javac -d out -cp "lib/*" @sources.txt

# 7️⃣ Setăm variabila de mediu pentru headless (fără GUI)
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"

# 8️⃣ Comanda default când rulăm containerul
CMD ["java", "-cp", "out:lib/*", "cinema.app.CinemaApp"]
