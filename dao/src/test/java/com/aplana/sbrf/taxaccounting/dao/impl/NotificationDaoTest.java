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
import java.util.Date;
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

    private Date date1 = new LocalDateTime(2013, 12, 31, 0, 0).toDate();

    @Test
    public void fetchOne() {
        Notification notification = notificationDao.fetchOne(1);
        assertEquals(NotificationBuilder.aNotification()
                        .id(1L).reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("asaa").logId("uuid_1")
                        .createDate(date1).deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(REF_BOOK_REPORT)
                        .build(),
                notification);
    }

    @Test
    public void fetchOne2() {
        Notification result = notificationDao.fetchOne(1, 2, 1);
        notNull(result);
    }

    @Test
    public void fetchEmpty() {
        Notification result = notificationDao.fetchOne(5, 2, 1);
        isNull(result);
    }

    @Test
    public void fetchForReceiverTest() {
        Notification notification = notificationDao.fetchOne(1);
        assertEquals(1L, notification.getId().longValue());
        assertEquals(1, notification.getReceiverDepartmentId().intValue());
        assertEquals(2, notification.getSenderDepartmentId().intValue());
    }

    @Test
    public void create() {
        NotificationBuilder notificationBuilder = NotificationBuilder.aNotification()
                .reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("text1").logId("uuid_1")
                .deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(REF_BOOK_REPORT);

        List<Notification> notifications = Arrays.asList(
                notificationBuilder.build(),
                notificationBuilder.but().text("text2").build());
        notificationDao.create(notifications);
        Notification notification = notificationDao.fetchOne(notifications.get(0).getId());
        assertEquals(notificationBuilder.but().id(notifications.get(0).getId()).createDate(notification.getCreateDate()).build(),
                notification);
        notification = notificationDao.fetchOne(notifications.get(1).getId());
        assertEquals(notificationBuilder.but().id(notifications.get(1).getId()).createDate(notification.getCreateDate()).text("text2").build(),
                notification);
    }

    @Test
    public void delete() {
        Notification result = notificationDao.fetchOne(1, 2, 1);
        notNull(result);
        result = notificationDao.fetchOne(1, 3, 1);
        notNull(result);
        List<DepartmentPair> departments = Arrays.asList(
                new DepartmentPair(2, 1),
                new DepartmentPair(3, 1),
                new DepartmentPair(null, 1));
        notificationDao.delete(1, departments);
        result = notificationDao.fetchOne(1, 2, 1);
        isNull(result);
        result = notificationDao.fetchOne(1, 3, 1);
        isNull(result);
        result = notificationDao.fetchOne(1, null, 1);
        isNull(result);
    }

    @Test
    public void fetchAllByFilter() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.fetchAllByFilter(filter).size());
        filter.setRead(null);
        assertEquals(3, notificationDao.fetchAllByFilter(filter).size());

        filter.setStartIndex(0);
        filter.setCountOfRecords(2);
        assertEquals(2, notificationDao.fetchAllByFilter(filter).size());

        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(2, notificationDao.fetchAllByFilter(userIdFilter).size());

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.fetchAllByFilter(userRoleFilter).size());

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.fetchAllByFilter(userAndRoleFilter).size());
    }

    @Test
    public void fetchAllByFilterAndPaging() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.fetchAllByFilterAndPaging(filter, PagingParams.getInstance(1, 1000)).size());
        filter.setRead(null);
        assertEquals(3, notificationDao.fetchAllByFilterAndPaging(filter, null).size());
        assertEquals(2, notificationDao.fetchAllByFilterAndPaging(filter, PagingParams.getInstance(1, 2)).size());
    }

    @Test
    public void fetchAllByFilterAndPaging2() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(2, notificationDao.fetchAllByFilterAndPaging(userIdFilter, null).size());
    }

    @Test
    public void fetchAllByFilterAndPaging3() {
        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.fetchAllByFilterAndPaging(userRoleFilter, null).size());
    }

    @Test
    public void fetchAllByFilterAndPaging4() {
        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.fetchAllByFilterAndPaging(userAndRoleFilter, null).size());
    }

    @Test
    public void fetchCountByFilter() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Arrays.asList(1));
        filter.setRead(true);
        assertEquals(2, notificationDao.fetchCountByFilter(filter));
        filter.setRead(null);
        assertEquals(3, notificationDao.fetchCountByFilter(filter));

        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertEquals(2, notificationDao.fetchCountByFilter(userIdFilter));

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertEquals(3, notificationDao.fetchCountByFilter(userRoleFilter));

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertEquals(4, notificationDao.fetchCountByFilter(userAndRoleFilter));
    }

    @Test
    public void deleteByReportPeriod() {
        assertEquals(new Integer(1), notificationDao.fetchOne(1).getReportPeriodId());
        notificationDao.deleteByReportPeriod(1);
        isNull(notificationDao.fetchOne(1));
    }

    @Test
    public void updateReadTrueByFilter() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        List<Notification> notifications = notificationDao.fetchAllByFilter(userIdFilter);
        assertFalse(notifications.get(1).isRead());
        notificationDao.updateReadTrueByFilter(userIdFilter);
        notifications = notificationDao.fetchAllByFilter(userIdFilter);
        assertTrue(notifications.get(1).isRead());
    }

    @Test
    public void deleteAll() {
        notNull(notificationDao.fetchOne(1));
        notNull(notificationDao.fetchOne(2));
        notificationDao.deleteAll(Arrays.asList(1L, 2L, 1234L));
        isNull(notificationDao.fetchOne(1));
        isNull(notificationDao.fetchOne(2));
    }

    @Test
    public void fetchLastNotificationDate() {
        assertEquals(date1,
                notificationDao.fetchLastNotificationDate());
    }
}
