package se.falukropp.lucene.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.falukropp.lucene.service.IndexDirectory;
import se.falukropp.lucene.service.IndexDirectory.SearchResult;
import se.falukropp.lucene.service.StorageFileNotFoundException;
import se.falukropp.lucene.service.StorageService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;

@RestController
public class WordSearchRestController {

    private static final Logger LOG = LoggerFactory.getLogger(WordSearchRestController.class);

    @Autowired
    private
    IndexDirectory indexDirectory;

    @Autowired
    private
    StorageService storageService;

    @PostConstruct
    public void init() {
        indexDirectory.indexDirectory(storageService.getRootLocation());
    }

    @RequestMapping(value = "/search/{word}")
    public SearchResult searchForWord(@PathVariable String word) {

        return indexDirectory.searchResult(word);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file,
                                              RedirectAttributes redirectAttributes) {

        try {
            Path uploadedFile = storageService.store(file);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + file.getOriginalFilename() + "!");
            indexDirectory.indexDoc(uploadedFile);
        } catch (IOException e) {
            LOG.error("Could not upload file " + file.getName(), e);
            throw new RuntimeException(e);
        }

        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
