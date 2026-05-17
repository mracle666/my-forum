FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache tzdata
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
