FROM java:8
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
VOLUME /var/lucene-storage
ENTRYPOINT ["/usr/bin/java", "-jar", "/maven/lucene.jar"]
ARG JAR_FILE
ADD target/${JAR_FILE} /maven/lucene.jar