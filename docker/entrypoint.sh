#!/usr/bin/env sh
set -e

# Spring Boot application entrypoint
# Environment variables are passed directly to Spring Boot via SPRING_* properties
exec java -jar /app/app.jar "$@"
