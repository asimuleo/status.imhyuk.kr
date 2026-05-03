# 빌드 스테이지
FROM gradle:9.4-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]