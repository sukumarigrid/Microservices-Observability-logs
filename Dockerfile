FROM maven:3.9.12-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /workspace/target/Microservices-Observability.jar /app/app.jar

ENV APP_NAME=Microservices-Observability

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
