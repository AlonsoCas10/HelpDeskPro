# Etapa 1: compilar. Aquí están el JDK y Maven; no llegan a la imagen final.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Etapa 2: ejecutar. Solo el JRE y el .jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home factsafe
COPY --from=build /app/target/*.jar app.jar
USER factsafe
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
