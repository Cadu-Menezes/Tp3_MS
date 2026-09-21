FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl config-server -am clean package -DskipTests

FROM eclipse-temurin:17-jre
RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/config-server/target/config-server-1.0.0.jar app.jar
COPY config-repo /app/config-repo
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
