FROM gradle:7.3.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -D io.u11.alerts2discord.ignoreversion=true

FROM openjdk:11-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/ /app/

WORKDIR /app
ENTRYPOINT ["java", "-Dio.u11.alerts2discord.disablesaving=true","-jar","/app/alerts2discord.jar"]
