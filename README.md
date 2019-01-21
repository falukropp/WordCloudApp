# Köra

Bara att starta DemoApplication

## För spotify docker build
```
$ ./mvnw clean install -DskipTests=true
$ docker run -it --rm -p8080:8080 falukropp/lucene:0.0.1-SNAPSHOT

$ docker run -dit  -p8080:8080 --log-driver gelf --log-opt gelf-address=udp://localhost:12201 falukropp/lucene:0.0.1-SNAPSHOT

```
 
## För fabricat8 docker build
```
$ ./mvnw clean package docker:build -DskipTests=true
$ docker run -it --rm -p8080:8080 my_lucene_tester:0.0.1-SNAPSHOT
``` 
