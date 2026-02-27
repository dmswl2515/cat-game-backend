# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle 의존성 레이어 캐싱
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 복사 및 빌드 (테스트 스킵)
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 8567

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
