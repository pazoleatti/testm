package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.builder.NotificationBuilder;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.NotificationType.REF_BOOK_REPORT;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.notNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NotificationDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationDaoTest {

    @Autowired
    NotificationDao notificationDao;

    private LocalDateTime date1 = new LocalDateTime(2013, 12, 31, 0, 0);

    @Test
    public void get() {
        Notification notification = notificationDao.get(1);
        assertEquals(NotificationBuilder.aNotification()
                        .id(1L).reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("asaa").logId("uuid_1")
                        .createDate(date1).deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(REF_BOOK_REPORT)
                        .build(),
                notification);
    }

    @Test
    public void get2() {
        Notification result = notificationDao.get(1, 2, 1);
        notNull(result);
    }

    @Test
    public void getEmpty() {
        Notification result = notificationDao.get(5, 2, 1);
        isNull(result);
    }

    @Test
    public void getForReceiverTest() {
        Notification notification = notificationDao.get(1);
        assertEquals(1L, notification.getId().longValue());
        assertEquals(1, notification.getReceiverDepartmentId().intValue());
        assertEquals(2, notification.getSenderDepartmentId().intValue());
    }

    @Test
    public void saveList() {
        NotificationBuilder notificationBuilder = NotificationBuilder.aNotification()
                .reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("text1").logId("uuid_1")
                .deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(REF_BOOK_REPORT);

        List<Notification> notifications = Arrays.asList(
                notificationBuilder.build(),
                notificationBuilder.but().text("text2").build());
        notificationDao.saveList(notifications);
        Notification notification = notificationDao.get(notifications.get(0).getId());
        assertEquals(notificationBuilder.but().id(notifications.get(0).getId()).createDate(notification.getCreateDate()).build(),
                notification);
        notification = notificationDao.get(notifications.get(1).getId());
        assertEquals(notificationBuilder.but().id(notifications.get(1).getId()).createDate(notification.getCreateDate()).text("text2").build(),
                notification);
    }

    @Test
    public void deleteListTest() {
        Notification result = notificationDao.get(1, 2, 1);
        notNull(result);
        result = notificationDao.get(1, 3, 1);
        notNull(result);
        List<DepartmentPair> departments = Arrays.asList(
                new DepartmentPair(2, 1),
                new DepartmentPair(3, 1),
                new DepartmentPair(null, 1));
        notificationDao.deleteList(1, departments);
        result = notificationDao.get(1, 2, 1);
        isNull(result);
        result = notificationDao.get(1, 3, 1);
        isNull(result);
        result = notificationDao.get(1, null, 1);
        isNull(result);
    }

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
        assertEquals(2, notificationDao.getByFilter(userIdFilter).size());

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.getByFilter(userRoleFilter).size());

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.getByFilter(userAndRoleFilter).size());
    }

    @Test
    public void getByFilterWithPaging1() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.getByFilterWithPaging(filter, PagingParams.getInstance(1, 1000)).size());
        filter.setRead(null);
        assertEquals(3, notificationDao.getByFilterWithPaging(filter, null).size());
        assertEquals(2, notificationDao.getByFilterWithPaging(filter, PagingParams.getInstance(1, 2)).size());
    }

    @Test
    public void getByFilterWithPaging2() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(2, notificationDao.getByFilterWithPaging(userIdFilter, null).size());
    }

    @Test
    public void getByFilterWithPaging3() {
        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.getByFilterWithPaging(userRoleFilter, null).size());
    }

    @Test
    public void getByFilterWithPaging4() {
        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.getByFilterWithPaging(userAndRoleFilter, null).size());
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
        assertEquals(2, notificationDao.getCountByFilter(userIdFilter));

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.getCountByFilter(userRoleFilter));

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.getCountByFilter(userAndRoleFilter));
    }

    @Test
    public void deleteByReportPeriod() {
        assertEquals(new Integer(1), notificationDao.get(1).getReportPeriodId());
        notificationDao.deleteByReportPeriod(1);
        isNull(notificationDao.get(1));
    }

    @Test
    public void updateUserNotificationsStatus() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        List<Notification> notifications = notificationDao.getByFilter(userIdFilter);
        assertFalse(notifications.get(1).isRead());
        notificationDao.updateUserNotificationsStatus(userIdFilter);
        notifications = notificationDao.getByFilter(userIdFilter);
        assertTrue(notifications.get(1).isRead());
    }

    @Test
    public void deleteAll() {
        notNull(notificationDao.get(1));
        notNull(notificationDao.get(2));
        notificationDao.deleteAll(Arrays.asList(1L, 2L, 1234L));
        isNull(notificationDao.get(1));
        isNull(notificationDao.get(2));
    }

    @Test
    public void getLastNotificationDate() {
        assertEquals(date1.toDate(),
                notificationDao.getLastNotificationDate());
    }
}
