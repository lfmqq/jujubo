FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata \
    && addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app

RUN chown -R spring:spring /app

COPY --from=builder --chown=spring:spring /workspace/target/springboot3-admin-*.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
