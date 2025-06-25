# Use the official Selenium standalone Chrome image as base
# Selenium 4.33.0 with Chrome 136.0.7103.113
FROM selenium/standalone-chrome:4.33.0-20250606

# Switch to root to install JDK 17 + Maven
USER root
RUN apt-get update \
 && apt-get install -y \
    openjdk-17-jdk \
    maven \
 && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME so Maven picks up JDK 17
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# Switch back to the selenium user
USER seluser

WORKDIR /home/seluser/project

# Copy pom only, so we can cache deps
COPY --chown=seluser:seluser pom.xml .

# Pre-fetch dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the code
COPY --chown=seluser:seluser . .

# No ENTRYPOINT—Compose will invoke Maven