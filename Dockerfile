FROM maven:latest AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM eclipse-temurin:17
EXPOSE 8282
RUN mkdir /opt/app
COPY --from=build /usr/src/app/target/memify-1.0.0.jar /opt/app
CMD ["java", "-jar", "/opt/app/memify-1.0.0.jar"]