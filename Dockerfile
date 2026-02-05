# ---------- Build ----------
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies

COPY src ./src
RUN gradle bootJar

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
