FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/*.jar app.jar
COPY env.properties env.properties

ENTRYPOINT ["java","-jar","app.jar"]