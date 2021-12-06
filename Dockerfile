FROM ubuntu:21.10
RUN apt update && apt install -y openjdk-17-jdk libopencv4.5-java

WORKDIR /app
COPY gradle gradlew settings.gradle.kts build.gradle.kts /app/
RUN chmod +x gradlew
RUN ./gradlew build || return 0

COPY . .
RUN ./gradlew clean build --console=plain --info \
    && cp build/libs/*.jar app.jar

CMD java -Xms64m -Xmx512m -jar app.jar
