# Dockerfile의 3번 줄을 다음과 같이 수정합니다.

# Java 17 기반의 JRE(실행 환경)만 포함된 경량 이미지를 사용합니다.
# OpenJDK 공식 이미지보다 Eclipse Temurin 이미지가 더 안정적으로 제공됩니다.
FROM eclipse-temurin:17-jre-focal

# 이전 코드: FROM openjdk:17-jdk-slim-buster (오류 발생)

# 2. 컨테이너 내부에서 사용할 볼륨 경로를 설정합니다. (선택 사항)
VOLUME /tmp

# 3. JAR 파일의 이름을 설정합니다.
ARG JAR_FILE=target/springboot-0.0.1-SNAPSHOT.jar

# 4. 호스트(PC)에 있는 JAR 파일을 컨테이너 내부의 app.jar로 복사합니다.
COPY ${JAR_FILE} app.jar

# 5. 스프링 부트 애플리케이션을 실행합니다.
ENTRYPOINT ["java","-jar","/app.jar"]