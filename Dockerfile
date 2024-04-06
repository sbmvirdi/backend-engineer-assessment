FROM azul/zulu-openjdk:21
EXPOSE 8081
ADD build/libs/app-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]