package se.falukropp.lucene.service;

// https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/main/java/hello/storage/StorageException.java
public class StorageException extends RuntimeException {

    private static final long serialVersionUID = -4963664044319905557L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}