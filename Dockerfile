FROM maven:3-openjdk-11 as build

COPY . .
RUN mvn clean install


FROM gcr.io/distroless/java-debian10:11 as run

COPY --from=build ./target/*.jar /app.jar
USER 65534:65534
CMD ["app.jar"]
