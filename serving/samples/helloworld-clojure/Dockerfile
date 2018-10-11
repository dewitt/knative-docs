FROM clojure AS build
WORKDIR /usr/src/app
COPY project.clj /usr/src/app
RUN lein deps
COPY . /usr/src/app
RUN lein uberjar

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /usr/src/app/target/uberjar/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
