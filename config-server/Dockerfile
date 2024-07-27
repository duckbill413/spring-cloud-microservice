FROM openjdk:17-ea-slim-buster
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks
COPY /build/libs/config-server-0.0.1-SNAPSHOT.jar ConfigServer.jar
ENTRYPOINT ["java", "-jar", "ConfigServer.jar"]