package se.falukropp.lucene.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/main/java/hello/storage/StorageProperties.java
@Component
@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}