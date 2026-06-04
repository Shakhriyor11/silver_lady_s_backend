# ---- Run stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user — xavfsizlik uchun
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

ENV PORT=8080
EXPOSE 8080

# +UseContainerSupport: JVM konteyner memory limitini hurmat qiladi
# MaxRAMPercentage: mavjud RAMning 75% ini heap uchun ajratadi
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "/app/app.jar"]
