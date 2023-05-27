FROM eclipse-temurin:17.0.7_7-jre-jammy

RUN mkdir -p /opt/app/responses
COPY target/*.jar /opt/app/app.jar

WORKDIR /opt/app

CMD ["java", "-jar", "/opt/app/app.jar"]
