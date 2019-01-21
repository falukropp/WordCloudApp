package se.falukropp.lucene.service;

// https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/main/java/hello/storage/StorageFileNotFoundException.java
public class StorageFileNotFoundException extends StorageException {

    /**
     *
     */
    private static final long serialVersionUID = -2084557292128243999L;

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}