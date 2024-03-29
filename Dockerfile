FROM ubuntu:21.10
RUN sed -i 's|http://archive.|http://ru.archive.|g' /etc/apt/sources.list \
    && apt update \
    && apt install -y openjdk-17-jdk libopencv4.5-java

WORKDIR /app
COPY gradle gradlew settings.gradle.kts build.gradle.kts /app/
RUN chmod +x gradlew
RUN ./gradlew build || return 0

COPY . .
RUN ./gradlew clean build --console=plain --info

FROM ubuntu:21.10
RUN sed -i 's|http://archive.|http://ru.archive.|g' /etc/apt/sources.list \
    && apt update \
    && apt install -y openjdk-17-jdk libopencv4.5-java
COPY --from=0 /app/build/libs/*.jar app.jar
CMD java -Xms64m -Xmx512m -jar app.jar
