# --- Stage 1: Build the jar ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 複製 pom.xml 和 src 資料夾
COPY pom.xml .
COPY src ./src

# 執行 Maven 打包
RUN mvn clean package -DskipTests

# --- Stage 2: Run the app ---
FROM eclipse-temurin:17-jdk

WORKDIR /app

# 從上一階段複製 jar
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
