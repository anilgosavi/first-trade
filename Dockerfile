# Use the official Maven image to create a build artifact
FROM maven:3.8.1-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use the official OpenJDK image to run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/tradestore-0.0.1-SNAPSHOT.jar tradestore.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tradestore.jar"]
