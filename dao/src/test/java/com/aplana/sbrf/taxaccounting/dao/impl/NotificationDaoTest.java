package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NotificationDaoTest.xml"})
@Transactional
public class NotificationDaoTest {

    @Autowired
    NotificationDao notificationDao;

    @Test
    public void saveTest() {
        Notification n = new Notification();
        n.setDeadline(new Date());
        n.setText("");
        n.setReceiverDepartmentId(1);
        n.setSenderDepartmentId(2);
        n.setReportPeriodId(1);
        n.setCreateDate(new Date());
        n.setFirstReaderId(1);
        Integer id = notificationDao.save(n);
        notNull(id);
    }

    @Test
    public void getTest() {
        Notification result = notificationDao.get(1, 2, 1);
        notNull(result);
    }

    @Test
    public void getEmptyTest() {
        Notification result = notificationDao.get(5, 2, 1);
        isNull(result);
    }

    @Test
    public void listByDepartmentsTest() {
        List<Notification> list = notificationDao.listByDepartments(2, 1);
        assertEquals(list.size(), 2);
    }

    @Test
    public void saveListTest() {
        List<Notification> list = new ArrayList<Notification>();
        Notification n1 = new Notification();
        n1.setDeadline(new Date());
        n1.setCreateDate(new Date());
        n1.setText("test1");
        n1.setReportPeriodId(3);
        n1.setSenderDepartmentId(3);
        n1.setReceiverDepartmentId(1);
        n1.setFirstReaderId(null);
        list.add(n1);

        Notification n2 = new Notification();
        n2.setDeadline(new Date());
        n2.setCreateDate(new Date());
        n2.setText("test2");
        n2.setReportPeriodId(4);
        n2.setSenderDepartmentId(3);
        n2.setReceiverDepartmentId(1);
        n2.setFirstReaderId(null);
        list.add(n2);

        notificationDao.saveList(list);
        List<Notification> list2 = notificationDao.listByDepartments(2, 1);
        assertEquals(list2.size(), 2);
    }

    @Test
    public void deleteTest() {
        Notification result = notificationDao.get(1, 2, 1);
        notNull(result);
        notificationDao.delete(1, 2, 1);
        result = notificationDao.get(1, 2, 1);
        isNull(result);
    }

    @Test
    public void deleteListTest() {
        Notification result = notificationDao.get(1, 2, 1);
        notNull(result);
        result = notificationDao.get(1, 3, 1);
        notNull(result);
        List<DepartmentPair> departments = new ArrayList<DepartmentPair>();
        departments.add(new DepartmentPair(2,1));
        departments.add(new DepartmentPair(3,1));
        notificationDao.deleteList(1, departments);
        result = notificationDao.get(1, 2, 1);
        isNull(result);
        result = notificationDao.get(1, 3, 1);
        isNull(result);
    }
}
