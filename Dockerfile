# Stage 1: build the jar. Uses a full JDK + Maven cache, discarded after this stage —
# none of this ends up in the final image, keeping it small.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# mvnw isn't tracked as executable in git (same issue as the CI workflow hit) —
# fixed here rather than in git itself, same reasoning as .github/workflows/ci.yml.
RUN chmod +x mvnw
# Downloads dependencies as their own layer, before copying source — so editing
# application code later doesn't force re-downloading the whole dependency tree.
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: just the JRE (no Maven, no JDK, no build tools) + the jar from stage 1.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
