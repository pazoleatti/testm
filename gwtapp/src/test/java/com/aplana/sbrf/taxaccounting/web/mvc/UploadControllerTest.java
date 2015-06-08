package com.aplana.sbrf.taxaccounting.web.mvc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "UploadControllerTest.xml")
@WebAppConfiguration
public class UploadControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private UploadController uploadController;

    @Before
    public void setup() {

        // Setup Spring test in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(uploadController).build();
    }

    @Test
    public void processUploadTempTest() throws Exception {
        File cf = File.createTempFile("controller", ".tmp");
        FileWriter outputStream = new FileWriter(cf);
        outputStream.write("->");
        outputStream.close();

        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

            mockMvc.perform(fileUpload("/uploadController/pattern").file(multipartFile)
                            .contentType(mediaType)
                            .param("description", "description")
                            .param("title", "title")
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("{uuid : \"null\"}"));
        } finally {
            if (!cf.delete())
                System.out.println("Can't delete");
        }

    }
}
