## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-native-image:20.3.3-java11 AS build
COPY gradlew /project/gradlew
COPY gradle /project/gradle
COPY build.gradle /project/
COPY settings.gradle /project/
COPY gradle.properties /project/
USER quarkus
WORKDIR /project
COPY src /project/src
RUN gradle -b /project/build.gradle buildNative

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY --from=build /project/build/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]