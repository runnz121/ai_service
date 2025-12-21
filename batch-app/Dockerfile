FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY batch-app/build.gradle.kts batch-app/

COPY batch-app/src batch-app/src

RUN gradle :batch-app:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/batch-app/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
