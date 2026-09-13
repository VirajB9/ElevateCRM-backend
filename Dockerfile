FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

RUN chmod +x gradlew && \
    ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:17-jre

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
