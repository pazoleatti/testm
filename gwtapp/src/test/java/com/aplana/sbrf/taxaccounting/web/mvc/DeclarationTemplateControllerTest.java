package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "DeclarationTemplateControllerTest.xml")
@WebAppConfiguration
public class DeclarationTemplateControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private DeclarationTemplateController declarationTemplateController;
    @Autowired
    SecurityService securityService;
    @Autowired
    DeclarationTemplateService declarationTemplateService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    BlobDataService blobDataService;

    @Before
    public void setup() {
        Mockito.when(securityService.currentUserInfo()).thenReturn(new TAUserInfo());

        // Setup Spring test in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(declarationTemplateController).build();
    }

    @Test
    public void uploadDectTest() throws Exception {
        DeclarationTemplate dt = new DeclarationTemplate();
        when(declarationTemplateService.get(1)).thenReturn(dt);

        File cf = File.createTempFile("dt_controller", ".tmp");
        FileWriter outputStream = new FileWriter(cf);
        outputStream.write("a");
        outputStream.close();

        String uuid = UUID.randomUUID().toString();
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn(uuid);

        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
            JSONObject expectedJson = new JSONObject();
            expectedJson.put(UuidEnum.SUCCESS_UUID.toString(), uuid);

            mockMvc.perform(fileUpload("/declarationTemplate/uploadDect/1").file(multipartFile)
                            .contentType(mediaType)
                            .param("description", "description")
                            .param("title", "title")
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(expectedJson.toString()));
        } finally {
            if (!cf.delete())
                System.out.println("Can't delete");
        }

    }

    @Test
    public void uploadDectFailTest() throws Exception {
        DeclarationTemplate dt = new DeclarationTemplate();
        when(declarationTemplateService.get(1)).thenReturn(dt);

        File cf = File.createTempFile("dt_controller", ".tmp");

        String uuid = UUID.randomUUID().toString();
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn(uuid);

        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
            JSONObject expectedJson = new JSONObject();
            expectedJson.put(UuidEnum.ERROR_UUID.toString(), uuid);

            mockMvc.perform(fileUpload("/declarationTemplate/uploadDect/1").file(multipartFile)
                            .contentType(mediaType)
                            .param("description", "description")
                            .param("title", "title")
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(expectedJson.toString()));
        } finally {
            if (!cf.delete())
                System.out.println("Can't delete");
        }

    }

    @Test
    public void uploadXsdTest() throws Exception {
        DeclarationTemplate dt = new DeclarationTemplate();
        when(declarationTemplateService.get(1)).thenReturn(dt);

        File cf = File.createTempFile("dt_controller", ".tmp");
        FileWriter outputStream = new FileWriter(cf);
        outputStream.write("a");
        outputStream.close();

        String uuid = UUID.randomUUID().toString();
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn(uuid);

        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
            JSONObject expectedJson = new JSONObject();
            expectedJson.put(UuidEnum.SUCCESS_UUID.toString(), uuid);

            mockMvc.perform(fileUpload("/uploadXsd/1").file(multipartFile)
                            .contentType(mediaType)
                            .param("description", "description")
                            .param("title", "title")
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(expectedJson.toString()));
        } finally {
            if (!cf.delete())
                System.out.println("Can't delete");
        }
    }

    @Test
    public void downloadTest() throws Exception {
        BlobData data = new BlobData();
        data.setName("привет.xsd");
        String s = "<name>HELLO!</nme>";
        String key = "Content-Disposition";
        String value = "attachment; filename=\"" + URLEncoder.encode(data.getName(), "UTF-8") + "\"";
        data.setInputStream(new ByteArrayInputStream(s.getBytes()));

        String uuid = UUID.randomUUID().toString();
        when(blobDataService.get(uuid)).thenReturn(data);
        MediaType expectedMT = new MediaType("text", "xml");
        mockMvc.perform(get(String.format("/downloadByUuid/%s", uuid)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(expectedMT))
                .andExpect(MockMvcResultMatchers.content().string(s))
                .andExpect(MockMvcResultMatchers.header().string(key, value));
    }
}
