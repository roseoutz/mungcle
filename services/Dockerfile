### 공유 멀티스테이지 Dockerfile — 서비스별로 SERVICE_NAME arg를 넘긴다.
### 사용: docker build --build-arg SERVICE_NAME=identity -f services/Dockerfile .
###
### proto codegen이 아직 안 되어 있으면 빌드 실패할 수 있음.
### docker-compose의 app 프로필은 codegen 완료 후 사용.

# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS builder

ARG SERVICE_NAME
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY common/ common/
COPY services/${SERVICE_NAME}/ services/${SERVICE_NAME}/

RUN chmod +x gradlew && \
    ./gradlew :services:${SERVICE_NAME}:bootJar --no-daemon -x test

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME
WORKDIR /app

COPY --from=builder /app/services/${SERVICE_NAME}/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
