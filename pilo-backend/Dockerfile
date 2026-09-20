FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/pilo-backend-*.jar app.jar
EXPOSE 8080
# t4g.micro = 1 GB RAM. SerialGC + MaxRAMPercentage leave room for the OS and SSH.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["java", "-jar", "app.jar"]
