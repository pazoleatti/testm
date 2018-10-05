package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.IdDocService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;


public class RefBookFlControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private RefBookFlController flController;
    @Mock
    private IdDocService idDocService;
    @Mock
    private SecurityService securityService;

    private TAUser anyUser;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(flController).build();

        anyUser = new TAUser();
        TAUserInfo anyUserInfo = new TAUserInfo();
        anyUserInfo.setUser(anyUser);
        when(securityService.currentUserInfo()).thenReturn(anyUserInfo);
    }

    @Test
    public void test_deleteIdDoc() throws Exception {

        long anyPersonId = 10;
        List<Long> docIds = asList(1L, 2L);

        doNothing().when(idDocService).deleteByIds(docIds, anyUser);

        mockMvc.perform(delete("/rest/refBookFL/{personId}/idDocs", anyPersonId)
                .param("id", "1", "2"))
                .andExpect(status().isNoContent());

        verify(idDocService, times(1)).deleteByIds(docIds, anyUser);
        verifyNoMoreInteractions(idDocService);
    }
}
