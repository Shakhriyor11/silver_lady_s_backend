# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Avval faqat pom.xml ni ko'chiramiz — dependency'lar cache qilinadi
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Keyin source kodini ko'chiramiz
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user — xavfsizlik uchun
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
