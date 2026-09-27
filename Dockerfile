FROM selenium/standalone-firefox:latest

USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY . .
RUN chown -R seluser:seluser /workspace
ENV MAVEN_OPTS=-Djava.awt.headless=true
USER seluser
CMD ["sh", "scripts/run-and-notify.sh", "-Pparallel"]
