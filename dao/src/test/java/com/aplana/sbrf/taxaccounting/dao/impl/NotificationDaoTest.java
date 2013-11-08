package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.Notification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
public class NotificationDaoTest {

    @Autowired
    NotificationDao notificationDao;

    /*@Test
    public void saveTest() {
        Notification n = new Notification();
        n.setDeadline(new Date());
        n.setText("");
        n.setReceiverDepartmentId(1);
        n.setSenderDepartmentId(2);
        n.setReportPeriodId(1);
        n.setCreateDate(new Date());
        n.setFirstReaderId(1);
        notificationDao.save(n);
    }*/
}
