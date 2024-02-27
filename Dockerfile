FROM maven:3.8.7-eclipse-temurin-19-alpine as build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests=true -Dmaven.javadoc.skip=true


FROM openjdk:19-alpine
RUN addgroup -S client && adduser -S client -G client
USER client:client
ARG DEPENDENCY=target/dependency
COPY --from=build /app/target/client*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]