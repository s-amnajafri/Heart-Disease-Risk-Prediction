FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app/backend
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app/backend
COPY --from=build /app/backend/target/heart-risk.jar app.jar
COPY data/heart.csv /app/data/heart.csv

EXPOSE 7071

CMD ["sh", "-c", "java -jar app.jar"]
