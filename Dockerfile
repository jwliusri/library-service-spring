FROM maven:3.9.8-eclipse-temurin-21 AS build

COPY src /app/src

COPY pom.xml /app

WORKDIR /app
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-alpine
COPY --from=build /app/target/*.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]