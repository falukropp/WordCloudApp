package se.falukropp.lucene;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DemoApplication.class);
        app.setBanner(new Banner() {
            @Override
            public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("banner.txt")))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        app.run(args);
    }

    // TODO: Make a Spring Shell REPL?

    // @Bean
    // public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    // return args -> {
    //
    // Analyzer analyzer = new StandardAnalyzer();
    //
    // // Store the index in memory:
    // Directory directory = new RAMDirectory();
    // // To store an index on disk, use this instead:
    // // Directory directory = FSDirectory.open("/tmp/testindex");
    // IndexWriterConfig config = new IndexWriterConfig(analyzer);
    // IndexWriter iwriter = new IndexWriter(directory, config);
    // iwriter.commit();
    //
    // Path docDir =
    // Paths.get(this.getClass().getResource("/ABunchOfTextfiles").toURI());
    // IndexDirectory.indexDirectory(iwriter, docDir);
    // iwriter.commit();
    //
    // // Now search the index:
    // DirectoryReader ireader = DirectoryReader.open(directory);
    //
    // System.out.println("Number of documents " +
    // ireader.getDocCount("contents"));
    // IndexSearcher isearcher = new IndexSearcher(ireader);
    // // Parse a simple query that searches for "text":
    // QueryParser parser = new QueryParser("contents", analyzer);
    // Query query = parser.parse("Version");
    // ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
    // System.out.println("Number of hits : " + hits.length);
    //
    // // Iterate through the results:
    // for (int i = 0; i < hits.length; i++) {
    // Document hitDoc = isearcher.doc(hits[i].doc);
    // System.out.println(hitDoc.get("path"));
    // }
    // ireader.close();
    // directory.close();
    //
    // };
    // }

}
