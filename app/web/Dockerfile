FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar app.jar

# Optional: enable remote debug with DEBUG_PORT
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