package se.falukropp.lucene.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// Based on https://github.com/spring-guides/gs-uploading-files/blob/master/complete/src/main/java/hello/storage/FileSystemStorageService.java
@Service
public class StorageService {

    private final Path rootLocation;

    @Autowired
    public StorageService(StorageProperties properties) {
        try {
            if (properties.getLocation() != null) {
                this.rootLocation = Paths.get(properties.getLocation());
                Files.createDirectories(this.rootLocation);
            } else {
                this.rootLocation = Files.createTempDirectory("jee_lucene");
            }
        } catch (IOException e) {
            throw new StorageException("Could not create/use storage directory", e);
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }

    public Path store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory " + filename);
            }
            Path filePath = rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

}
