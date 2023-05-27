FROM eclipse-temurin:17.0.7_7-jre-jammy

RUN mkdir /opt/app
COPY target/*.jar /opt/app/app.jar

CMD ["java", "-jar", "/opt/app/app.jar"]
