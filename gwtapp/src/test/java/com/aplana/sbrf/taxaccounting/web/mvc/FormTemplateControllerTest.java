package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.json.JSONObject;
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
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "FormTemplateControllerTest.xml")
@WebAppConfiguration
public class FormTemplateControllerTest {
    private MockMvc mockMvc;

    @Autowired
    FormTemplateController formTemplateController;
    @Autowired
    FormTemplateService formTemplateService;
    @Autowired
    SecurityService securityService;
    @Autowired
    FormTemplateImpexService formTemplateImpexService;
    @Autowired
    LogEntryService logEntryService;

    @Before
    public void setup() {
        when(securityService.currentUserInfo()).thenReturn(new TAUserInfo());

        // Setup Spring test in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(formTemplateController).build();
    }

    @Test
    public void uploadTest() throws Exception {
        File cf = File.createTempFile("controller", ".tmp");
        FileWriter outputStream = new FileWriter(cf);
        outputStream.write("a");
        outputStream.close();

        when(formTemplateService.getFTEndDate(1)).thenReturn(new Date());
        String uuid = UUID.randomUUID().toString();
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn(uuid);
        try{
            FileInputStream fis = new FileInputStream(cf);
            MockMultipartFile multipartFile = new MockMultipartFile("uploader", "filename.txt", "text/plain", fis);
            when(formTemplateImpexService.importFormTemplate(1, multipartFile.getInputStream())).thenReturn(new FormTemplate());

            HashMap<String, String> contentTypeParams = new HashMap<String, String>();
            contentTypeParams.put("boundary", "265001916915724");
            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

            JSONObject expectedJson = new JSONObject();
            expectedJson.put(UuidEnum.SUCCESS_UUID.toString(), uuid);
            mockMvc.perform(fileUpload("/formTemplate/upload/1").file(multipartFile)
                            .contentType(mediaType)
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(expectedJson.toString()));
        } finally {
            if (!cf.delete())
                System.out.println("Can't delete");
        }

    }

}
