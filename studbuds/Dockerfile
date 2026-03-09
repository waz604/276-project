FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

RUN mvn -f studbuds/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
COPY --from=build /app/studbuds/target/studbuds-0.0.1-SNAPSHOT.jar studbuds.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "studbuds.jar"]