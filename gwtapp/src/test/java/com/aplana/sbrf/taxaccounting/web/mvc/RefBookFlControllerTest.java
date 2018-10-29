package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.IdDocService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


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

}
