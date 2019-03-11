# WordCloudApp

Silly little thing that just wires a Spring Boot Rest Controller to Lucene and then uses d3.js + d3-cloud to make word clouds

Also played around with docker and gelf logging at the same time, so that's included too!

## Run

Just start LuceneWordCloudApp and goto localhost:8080

## For spotify docker build
```
$ ./mvnw clean install -DskipTests=true
$ docker run -it --rm -p8080:8080 falukropp/lucene:0.0.1-SNAPSHOT

$ docker run -dit  -p8080:8080 --log-driver gelf --log-opt gelf-address=udp://localhost:12201 falukropp/lucene:0.0.1-SNAPSHOT

```
 
## For fabricat8 docker build
```
$ ./mvnw clean package docker:build -DskipTests=true
$ docker run -it --rm -p8080:8080 my_lucene_tester:0.0.1-SNAPSHOT
``` 
