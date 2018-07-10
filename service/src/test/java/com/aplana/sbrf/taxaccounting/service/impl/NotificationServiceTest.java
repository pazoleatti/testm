package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("NotificationServiceTest.xml")
public class NotificationServiceTest {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private BlobDataService blobDataService;

    @Test
    public void getNotificationBlobDataTest() {
        String blobId = "blobId";
        notificationService.getNotificationBlobData(blobId);
        Mockito.verify(blobDataService, Mockito.times(1)).get(blobId);
    }
}
