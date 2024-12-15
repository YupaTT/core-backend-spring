# syntax=docker/dockerfile:1.7
ARG JAVA_VERSION=21.0.11_10

# ---------- build ----------
FROM eclipse-temurin:${JAVA_VERSION}-jdk-noble AS build
WORKDIR /workspace
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.welcome=never"

# Dependencias en una capa separada: solo se re-descargan si cambian los archivos de build
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --configuration runtimeClasspath -q > /dev/null

COPY src/main src/main
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test -q \
    && java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# ---------- runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre-noble AS runtime

ARG VERSION=dev
ARG REVISION=unknown
LABEL org.opencontainers.image.title="core-backend-spring" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}"

# UID fijo para runAsNonRoot en Kubernetes
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app \
    && mkdir -p /app/storage-data \
    && chown app:app /app/storage-data

WORKDIR /app
# De la capa que menos cambia a la que más, para aprovechar la caché de capas
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./

ENV TZ=UTC \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    APP_INTEGRATIONS_STORAGE_LOCAL_ROOT=/app/storage-data \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -XX:+UseG1GC -Djava.io.tmpdir=/tmp"

USER app
EXPOSE 8080
VOLUME ["/app/storage-data"]

# Sin curl en la imagen: petición HTTP con /dev/tcp de bash
HEALTHCHECK --interval=15s --timeout=3s --start-period=90s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT} \
        && printf "GET /api/actuator/health/liveness HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3 \
        && grep -q "\"UP\"" <&3'

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
