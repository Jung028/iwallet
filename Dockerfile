FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
ENV GITHUB_USERNAME=$GITHUB_USERNAME
ENV GITHUB_TOKEN=$GITHUB_TOKEN

COPY . .
RUN mvn -B -DskipTests clean install -Pproduction

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/app/web/target/*.jar app.jar

ENV DEBUG_PORT=""

ENTRYPOINT ["sh", "-c", "\
if [ -n \"$DEBUG_PORT\" ]; then \
  echo 'Starting app with remote debug on port '$DEBUG_PORT; \
  exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT -jar /app/app.jar; \
else \
  echo 'Starting app normally'; \
  exec java -jar /app/app.jar; \
fi \
"]
