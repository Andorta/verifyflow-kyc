FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw \
        --batch-mode \
        --no-transfer-progress \
        dependency:go-offline

COPY src/ src/

RUN ./mvnw \
        --batch-mode \
        --no-transfer-progress \
        clean package \
        -DskipTests \
    && cp target/*.jar target/application.jar


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S verifyflow \
    && adduser -S verifyflow -G verifyflow

WORKDIR /app

COPY --from=build \
    --chown=verifyflow:verifyflow \
    /workspace/target/application.jar \
    /app/application.jar

USER verifyflow

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/application.jar"]