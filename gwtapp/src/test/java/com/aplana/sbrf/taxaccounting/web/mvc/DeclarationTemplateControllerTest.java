package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Matchers.*;
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
    @Autowired
    DeclarationTemplateImpexService declarationTemplateImpexService;

    @Before
    public void setup() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        TARole role = new TARole(){{
            setAlias(TARole.N_ROLE_CONF);
        }};
        user.setRoles(Arrays.asList(role));
        userInfo.setUser(user);
        Mockito.when(securityService.currentUserInfo()).thenReturn(userInfo);

        // Setup Spring test in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(declarationTemplateController).build();
    }

    @Test
    public void uploadDectTest() throws Exception {
        DeclarationTemplate dt = new DeclarationTemplate();
        dt.setStatus(VersionedObjectStatus.DRAFT);
        when(declarationTemplateService.get(1)).thenReturn(dt);
        when(declarationTemplateImpexService.importDeclarationTemplate(any(TAUserInfo.class), anyInt(), any(FileInputStream.class)))
                .thenReturn(dt);

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
    public void uploadDectIfJrxmlExistTest() throws Exception {
        DeclarationTemplate dt = new DeclarationTemplate();
        dt.setId(1);
        dt.setStatus(VersionedObjectStatus.DRAFT);
        dt.setJrxmlBlobId(UUID.randomUUID().toString());
        when(declarationTemplateImpexService.importDeclarationTemplate(any(TAUserInfo.class), anyInt(), any(FileInputStream.class)))
                .thenReturn(dt);
        when(declarationTemplateService.checkExistingDataJrxml(anyInt(), any(Logger.class)))
                .thenReturn(true);

        File cf = File.createTempFile("dt_controller", ".tmp");
        FileWriter outputStream = new FileWriter(cf);
        outputStream.write("a");
        outputStream.close();

        String uuid = UUID.randomUUID().toString();
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn(uuid);
        when(blobDataService.create(any(FileInputStream.class), anyString())).thenReturn(uuid);

        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
            JSONObject expectedJson = new JSONObject();
            expectedJson.put(UuidEnum.ERROR_UUID.toString(), uuid);
            expectedJson.put(UuidEnum.UPLOADED_FILE.toString(), uuid);

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
        String value = "attachment;filename=\"" + URLEncoder.encode(data.getName(), "UTF-8").replaceAll("\\+", "%20") + "\"";
        data.setInputStream(new ByteArrayInputStream(s.getBytes()));

        String uuid = UUID.randomUUID().toString();
        when(blobDataService.get(uuid)).thenReturn(data);
        mockMvc.perform(get(String.format("/downloadByUuid/%s", uuid)).header("User-Agent", "msie"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(s))
                .andExpect(MockMvcResultMatchers.header().string(key, value));
    }
}
