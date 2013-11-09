package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.Notification;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationServiceImplTest {
    private static NotificationServiceImpl service;

    @BeforeClass
    public static void init() {
        service = new NotificationServiceImpl();
    }

    @Test
    public void mapByDepartmentsTest() {
        NotificationDao notificationDao = mock(NotificationDao.class);
        ReflectionTestUtils.setField(service, "notificationDao", notificationDao);

        List<Notification> list = new ArrayList<Notification>();
        Notification n1 = new Notification();
        n1.setDeadline(new Date());
        n1.setCreateDate(new Date());
        n1.setText("test1");
        n1.setReportPeriodId(1);
        n1.setSenderDepartmentId(1);
        n1.setReceiverDepartmentId(1);
        n1.setFirstReaderId(null);
        list.add(n1);

        Notification n2 = new Notification();
        n2.setDeadline(new Date());
        n2.setCreateDate(new Date());
        n2.setText("test2");
        n2.setReportPeriodId(2);
        n2.setSenderDepartmentId(1);
        n2.setReceiverDepartmentId(1);
        n2.setFirstReaderId(null);
        list.add(n2);
        when(notificationDao.listByDepartments(1, 1)).thenReturn(list);

        Map<Integer, Notification> map = service.mapByDepartments(1, 1);
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey(1));
        Assert.assertTrue(map.containsKey(2));
    }
}
