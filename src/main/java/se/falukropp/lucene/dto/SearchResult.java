package se.falukropp.lucene.dto;

import java.util.List;

public class SearchResult {
    private final List<String> files;

    private final long totalHits;

    public SearchResult(List<String> files, long totalHits) {
        this.files = files;
        this.totalHits = totalHits;
    }

    public List<String> getFile() {
        return files;
    }

    public long getTotalHits() {
        return totalHits;
    }
}
