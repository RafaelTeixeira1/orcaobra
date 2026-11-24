# Etapa 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY orcaobra-core/pom.xml orcaobra-core/
COPY orcaobra-api/pom.xml orcaobra-api/
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY . .
RUN mvn -B -q package -DskipTests

# Etapa 2: execução
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/orcaobra-api/target/orcaobra-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
