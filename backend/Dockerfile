# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar \
    && cp "$(find build/libs -name '*.jar' ! -name '*-plain.jar' -print -quit)" /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system spring && useradd --system --gid spring spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /workspace/app.jar ./app.jar

USER spring:spring

ENV PORT=10000
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -Xss512k -XX:MaxMetaspaceSize=96m -XX:ReservedCodeCacheSize=48m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
