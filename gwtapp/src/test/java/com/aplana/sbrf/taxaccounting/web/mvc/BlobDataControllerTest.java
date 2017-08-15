package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "BlobDataControllerTest.xml")
@WebAppConfiguration
public class BlobDataControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private BlobDataController blobDataController;

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
        this.mockMvc = standaloneSetup(blobDataController).build();
    }


    @Test
    public void downloadTest() throws Exception {
        BlobData data = new BlobData();
        data.setName("привет.xsd");
        String s = "<name>HELLO!</nme>";
        String key = "Content-Disposition";
        String value = "attachment;filename=\"" + URLEncoder.encode(data.getName(), UTF_8).replaceAll("\\+", "%20") + "\"";
        data.setInputStream(new ByteArrayInputStream(s.getBytes()));

        String uuid = UUID.randomUUID().toString();
        when(blobDataService.get(uuid)).thenReturn(data);
        mockMvc.perform(get(String.format("/rest/blobData/%s/conf", uuid)).header("User-Agent", "msie"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(s))
                .andExpect(MockMvcResultMatchers.header().string(key, value));
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

            mockMvc.perform(fileUpload("/actions/blobData/uploadFile").file(multipartFile)
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
