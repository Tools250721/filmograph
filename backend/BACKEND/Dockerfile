# 멀티 스테이지 빌드: 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# pom.xml과 소스 코드 복사
COPY pom.xml .
COPY src ./src

# Maven 빌드 (의존성 다운로드 및 JAR 생성)
RUN mvn clean package -DskipTests

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/target/*.jar app.jar

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]


