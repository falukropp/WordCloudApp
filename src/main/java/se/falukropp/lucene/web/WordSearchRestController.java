package se.falukropp.lucene.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.falukropp.lucene.dto.CommonWordResult;
import se.falukropp.lucene.dto.SearchResult;
import se.falukropp.lucene.service.IndexDirectory;
import se.falukropp.lucene.service.StorageService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;

@CrossOrigin
@RestController
public class WordSearchRestController {

    private static final Logger LOG = LoggerFactory.getLogger(WordSearchRestController.class);

    @Autowired
    private
    IndexDirectory indexDirectory;

    @Autowired
    private
    StorageService storageService;

    @GetMapping(value = "/search/{word}")
    public SearchResult searchForWord(@PathVariable String word) {

        return indexDirectory.searchResult(word);
    }

    @GetMapping(value = "/word_count/{n}")
    public List<CommonWordResult> wordCount(@PathVariable Integer n) {

        return indexDirectory.mostCommon(n);
    }

    @GetMapping(value = "/filelist")
    public SortedSet<String> filelist() {

        return indexDirectory.getAllFiles();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("files") MultipartFile[] files) {

        for (MultipartFile file : files) {
            try {
                Path uploadedFile = storageService.store(file);
                indexDirectory.indexSingleDoc(uploadedFile);
            } catch (IOException e) {
                LOG.error("Could not upload file " + file.getName(), e);
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.accepted().build();
    }

}
