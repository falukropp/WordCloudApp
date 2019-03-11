package se.falukropp.lucene.dto;

public class CommonWordResult {
    private final String word;

    private final long totalHits;


    public String getWord() {
        return word;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public CommonWordResult(String word, long totalHits) {
        this.word = word;
        this.totalHits = totalHits;
    }
}
