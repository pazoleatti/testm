package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("RefBookServiceTest.xml")
public class RefBookServiceTest {
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookService refBookService;

    @Test
    public void testGetAdministrationSettingsBlobData() {
        TAUser user = mock(TAUser.class);
        String blobId = "blobId";
        refBookService.getAdministrationSettingsBlobData(blobId, user);
        Mockito.verify(blobDataService, Mockito.times(1)).get(blobId);
    }
}
