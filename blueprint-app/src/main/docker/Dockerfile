FROM openjdk:8-jdk-alpine

# Get bash
RUN apk add --no-cache bash gawk sed grep bc coreutils

# Mount tmp folder for logs etc
VOLUME /tmp

# Add the application files and go!
ADD start.sh start.sh
ADD maven/*.jar app.jar
ENTRYPOINT ["/bin/bash","start.sh"]