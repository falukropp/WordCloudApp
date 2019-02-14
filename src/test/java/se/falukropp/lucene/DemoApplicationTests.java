package se.falukropp.lucene;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import se.falukropp.lucene.service.IndexDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
public class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndexDirectory indexDirectory;

    @Before
    public void resetAndIndexSampleTextFiles() {
        // Needs to be reset between each test.
        indexDirectory.init();
        indexDirectory.indexDirectory(Paths.get("src/test/resources/SampleTextFiles/"));
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void testSimpleSearches() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/search/arrival").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHits").value(46));
        // .andDo(print());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/search/anchor").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHits").value(2));
    }

    @Test
    public void testUpload() throws Exception {
        String uploadedFileName = "Heart_of_Darkness.txt";

        Path uploadFilePath = Paths.get("src/test/resources/filesToBeUploaded/" + uploadedFileName);

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", uploadedFileName,
                "text/plain", Files.readAllBytes(uploadFilePath));

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/upload")
                        .file(mockMultipartFile)
        ).andExpect(status().isAccepted());


        // ...arrival isn't used once in uploaded file ""Heart_of_Darkness.txt"
        mockMvc.perform(
                MockMvcRequestBuilders.get("/search/arrival").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHits").value(46));

        // ... but anchor is, 3 times ...
        mockMvc.perform(
                MockMvcRequestBuilders.get("/search/anchor").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHits").value(5));
    }
}
