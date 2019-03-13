package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;


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
        Notification notification = new Notification();
        notification.setReportId(blobId);
        notification.setUserId(0);
        notificationService.getNotificationBlobData(notification);
        verify(blobDataService, times(1)).get(blobId);
    }
}
