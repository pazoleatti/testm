package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.notNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NotificationDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationDaoTest {

    @Autowired
    NotificationDao notificationDao;

    @Test
    public void getByFilterTest() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.getByFilter(filter).size());
        filter.setRead(null);
        assertEquals(3, notificationDao.getByFilter(filter).size());


        filter.setStartIndex(0);
        filter.setCountOfRecords(2);
        assertEquals(2, notificationDao.getByFilter(filter).size());

        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(1, notificationDao.getByFilter(userIdFilter).size());

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2,3));
        assertEquals(3, notificationDao.getByFilter(userRoleFilter).size());

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2,3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.getByFilter(userAndRoleFilter).size());
    }

    @Test
    public void getCountByFilterTest() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.getCountByFilter(filter));
        filter.setRead(null);
        assertEquals(3, notificationDao.getCountByFilter(filter));

        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(1, notificationDao.getCountByFilter(userIdFilter));

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2,3));
        assertEquals(3, notificationDao.getCountByFilter(userRoleFilter));

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2,3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.getCountByFilter(userAndRoleFilter));
    }

    @Test
    public void saveTest() {
        Notification n = new Notification();
        n.setDeadline(new Date());
        n.setText("");
        n.setReceiverDepartmentId(1);
        n.setSenderDepartmentId(2);
        n.setReportPeriodId(1);
        n.setCreateDate(new Date());
        n.setLogId("uuid_1");
        n.setReportId("uuid_2");
        n.setNotificationType(NotificationType.REF_BOOK_REPORT);
        Long id = notificationDao.save(n);
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
    public void saveListTest() {
        List<Notification> list = new ArrayList<Notification>();
        Notification n1 = new Notification();
        n1.setDeadline(new Date());
        n1.setCreateDate(new Date());
        n1.setText("test1");
        n1.setReportPeriodId(3);
        n1.setSenderDepartmentId(3);
        n1.setReceiverDepartmentId(1);
        list.add(n1);

        Notification n2 = new Notification();
        n2.setDeadline(new Date());
        n2.setCreateDate(new Date());
        n2.setText("test2");
        n2.setReportPeriodId(4);
        n2.setSenderDepartmentId(3);
        n2.setReceiverDepartmentId(1);
        list.add(n2);

        notificationDao.saveList(list);
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setSenderDepartmentId(2);
        List<Notification> list2 = notificationDao.getByFilter(filter);
        assertEquals(5, list2.size());
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
        departments.add(new DepartmentPair(null,1));
        notificationDao.deleteList(1, departments);
        result = notificationDao.get(1, 2, 1);
        isNull(result);
        result = notificationDao.get(1, 3, 1);
        isNull(result);
        result = notificationDao.get(1, null, 1);
        isNull(result);
    }


	@Test
	public void getForReceiverTest() {
		Notification notification = notificationDao.get(1);
		assertEquals(1L, notification.getId().longValue());
		assertEquals(1, notification.getReceiverDepartmentId().intValue());
		assertEquals(2, notification.getSenderDepartmentId().intValue());
	}
}
