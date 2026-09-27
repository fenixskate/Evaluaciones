FROM maven:3.9.15-eclipse-temurin-21
WORKDIR /workspace
COPY pom.xml .
COPY .mvn .mvn
RUN mvn -s .mvn/settings.xml -B dependency:go-offline
COPY . .
CMD ["sh", "scripts/run-tests.sh"]
