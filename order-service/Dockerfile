FROM openjdk:17-ea-33-jdk-slim

VOLUME /tmp

COPY ./build OrderService.jar

ENTRYPOINT ["java", "-jar", "OrderService.jar"]