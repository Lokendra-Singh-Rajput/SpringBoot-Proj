FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline -B || true

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jre AS runner

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

RUN mkdir -p /app/uploads/images /app/vector && chown -R appuser:appgroup /app

COPY --from=builder /build/target/SpringEcom-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown appuser:appgroup /app/app.jar

USER appuser

EXPOSE 8080

ENV SERVER_PORT=8080 \
    IMAGE_UPLOAD_DIR=/app/uploads/images \
    VECTOR_STORE_PATH=/app/vector-store.json \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/api/products?page=0&size=1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
