FROM ubuntu:latest
LABEL authors="sotaro"

FROM openjdk:17-jdk-alpine

WORKDIR /app

# run "mvn clean package" -> generate jar file
COPY target/payment-app.jar /app/payment-app.jar

EXPOSE 8082

# run the exeutable file
CMD ["java", "-jar", "/app/payment-app.jar"]