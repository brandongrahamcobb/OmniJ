FROM eclipse-temurin:23

# Set working directory
WORKDIR /app

# Copy your built .jar
COPY target/Application-0.1.jar Application.jar

# Copy the source files (adjust path to match your real project layout)
COPY src/main/java/com/brandongcobb/ /app/source/

# Optional: copy other things like resources or entrypoint script
COPY entry.sh .
RUN apt-get update && apt-get install -y wget curl bash

RUN wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq && \
    chmod +x /usr/local/bin/yq && chmod +x entry.sh

# Entry point
ENTRYPOINT ["bash", "entry.sh"]
