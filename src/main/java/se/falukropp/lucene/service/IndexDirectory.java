package se.falukropp.lucene.service;

/* Started as on https://lucene.apache.org/core/6_6_0/demo/src-html/org/apache/lucene/demo/IndexFiles.html
 *  Has been changed a lot since then.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.falukropp.lucene.dto.CommonWordResult;
import se.falukropp.lucene.dto.SearchResult;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.util.*;

/**
 * Index all text files under a directory.
 * <p>
 * TODO : Split indexing and searching?
 */
@Component
public class IndexDirectory {

    static private final Logger LOG = LoggerFactory.getLogger(IndexDirectory.class);

    private IndexWriter iwriter;
    private Directory directory;
    private Analyzer analyzer;

    @PostConstruct
    public void init() {
        try {
            CharArraySet stopWords = new CharArraySet(StandardAnalyzer.ENGLISH_STOP_WORDS_SET, true);
            for (char c = 'a'; c <= 'z'; c++) {
                if (c != 'i') {
                    stopWords.add(c);
                }
            }
            for (char c = '0'; c <= '9'; c++) {
                stopWords.add(c);
            }
            analyzer = new StandardAnalyzer(stopWords);
            directory = new ByteBuffersDirectory();
            // To store an index on disk, use this instead:
            // Directory directory = FSDirectory.open("/tmp/testindex");
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            iwriter = new IndexWriter(directory, config);
            iwriter.commit();

        } catch (IOException e) {
            LOG.error("Failed init", e);
        }
    }

    public void indexDirectory(Path docDir) {
        if (!Files.isReadable(docDir)) {
            LOG.error("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable.");
            System.exit(1);
        }

        Date start = new Date();
        try {
            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer. But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            indexAllDocsUnderDir(docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here. This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            iwriter.commit();

            Date end = new Date();
            LOG.info("Index directory : " + docDir);
            LOG.debug(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            LOG.error("Could not index directory: " + docDir.toString(), e);
        }

    }

    // TODO : This type of method should probably be moved to storage service instead.

    /***
     * <p>
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     * <p>
     * NOTE: This method indexes one document per input file. This is slow. For good
     * throughput, put multiple documents into your input file(s).
     *
     * @param path The file to index, or the directory to recurse into to find
     *             files to index
     * @throws IOException If there is a low-level I/O error
     */
    private void indexAllDocsUnderDir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        indexSingleDoc(file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexSingleDoc(path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    public void indexSingleDoc(Path file) throws IOException {
        indexSingleDoc(file, Clock.systemDefaultZone().millis());
        iwriter.commit();
    }

    /**
     * Indexes a single document
     */
    private void indexSingleDoc(Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            // make a new, empty document
            Document doc = new Document();

            // Add the path of the file as a field named "path". Use a
            // field that is indexed (i.e. searchable), but don't tokenize
            // the field into separate words and don't index term frequency
            // or positional information:
            Field pathField = new StringField("path", file.getFileName().toString(), Field.Store.YES);
            doc.add(pathField);

            // Add the last modified date of the file a field named "modified".
            // Use a LongPoint that is indexed (i.e. efficiently filterable with
            // PointRangeQuery). This indexes to milli-second resolution, which
            // is often too fine. You could instead create a number based on
            // year/month/day/hour/minutes/seconds, down the resolution you
            // require.
            // For example the long value 2011021714 would mean
            // February 17, 2011, 2-3 PM.
            doc.add(new LongPoint("modified", lastModified));

            // Add the contents of the file to a field named "contents". Specify
            // a Reader,
            // so that the text of the file is tokenized and indexed, but not
            // stored.
            // Note that FileReader expects the file to be in UTF-8 encoding.
            // If that's not the case searching for special characters will
            // fail.
            doc.add(new TextField("contents",
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            if (iwriter.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can
                // be there):
                LOG.info("adding file : " + file);
                iwriter.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been
                // indexed) so
                // we use updateDocument instead to replace the old one matching
                // the exact
                // path, if present:
                LOG.info("updating " + file);
                iwriter.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }

    public List<CommonWordResult> mostCommon(int n) {
        List<CommonWordResult> results = Collections.emptyList();
        try {
            DirectoryReader reader = DirectoryReader.open(directory);
            HighFreqTerms.TotalTermFreqComparator cmp = new HighFreqTerms.TotalTermFreqComparator();
            TermStats[] highFreqTerms = HighFreqTerms.getHighFreqTerms(reader, n, "contents", cmp);

            results = new ArrayList<>(highFreqTerms.length);
            for (TermStats ts : highFreqTerms) {
                results.add(new CommonWordResult(ts.termtext.utf8ToString(), ts.totalTermFreq));
            }
        } catch (Exception e) {
            LOG.error("Could not get most common words : ", e);
        }
        return results;
    }

    public SortedSet<String> getAllFiles() {
        SortedSet<String> fileNames = new TreeSet<>();
        try {
            DirectoryReader ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);

            Query query = new MatchAllDocsQuery();
            TopDocs hits = isearcher.search(query, 1000);

            // Iterate through the results:
            for (ScoreDoc hit : hits.scoreDocs) {
                Document hitDoc = isearcher.doc(hit.doc);
                fileNames.add(hitDoc.get("path"));
            }
            ireader.close();


        } catch (IOException e) {
            LOG.error("Could not get all files", e);
        }

        return fileNames;
    }


    public SearchResult searchResult(String word) {
        LOG.debug("Searching for " + word);

        DirectoryReader ireader;
        long totalHits = 0;
        List<String> fileNames = new ArrayList<>();

        try {
            ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse(word);
            TopDocs topDocs = isearcher.search(query, 1000);

            Term term = new Term("contents", word.toLowerCase());
            totalHits = ireader.totalTermFreq(term);

            ScoreDoc[] hits = topDocs.scoreDocs;

            // Iterate through the results:
            for (ScoreDoc hit : hits) {
                Document hitDoc = isearcher.doc(hit.doc);
                fileNames.add(hitDoc.get("path"));
            }
            ireader.close();

        } catch (IOException | ParseException e) {
            LOG.error("Could not search for word : " + word, e);
        }
        LOG.debug("Total number of hits : " + totalHits + " in the following files : " + String.join(",", fileNames));

        return new SearchResult(fileNames, totalHits);

    }

}
