package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.builder.NotificationBuilder;
import com.aplana.sbrf.taxaccounting.model.*;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NotificationDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationDaoTest {

    @Autowired
    NotificationDao notificationDao;

    private Date date1 = new LocalDateTime(2013, 12, 31, 0, 0).toDate();


    @Test
    public void test_create() {
        NotificationBuilder notificationBuilder = NotificationBuilder.aNotification()
                .reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("text1").logId("uuid_1")
                .deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(NotificationType.REF_BOOK_REPORT);

        List<Notification> notifications = Arrays.asList(
                notificationBuilder.build(),
                notificationBuilder.but().text("text2").build());
        notificationDao.create(notifications);

        Notification notification = notificationDao.findById(notifications.get(0).getId());
        assertThat(notification).isEqualTo(notificationBuilder.but().id(notifications.get(0).getId()).createDate(notification.getCreateDate()).build());

        notification = notificationDao.findById(notifications.get(1).getId());
        assertThat(notification).isEqualTo(notificationBuilder.but().id(notifications.get(1).getId()).createDate(notification.getCreateDate()).text("text2").build());
    }


    @Test
    public void test_findById() {
        Notification found = notificationDao.findById(1);
        Notification expected = NotificationBuilder.aNotification()
                .id(1L).reportPeriodId(1).senderDepartmentId(2).receiverDepartmentId(1).text("asaa").logId("uuid_1")
                .createDate(date1).deadline(date1).userId(1).roleId(1).read(true).reportId("uuid_2").notificationType(NotificationType.REF_BOOK_REPORT)
                .build();
        assertThat(found).isEqualTo(expected);
    }


    @Test
    public void test_findByIdIn() {
        List<Notification> result = notificationDao.findByIdIn(Arrays.asList(1L, 2L, 3L));
        assertThat(result)
                .isNotEmpty()
                .extracting("id")
                .containsOnly(1L, 2L, 3L);
    }


    @Test
    public void test_findByReportPeriodAndDepartments_forExistent() {
        Notification result = notificationDao.findByReportPeriodAndDepartments(1, 2, 1);
        assertThat(result).isNotNull();
    }

    @Test
    public void test_findByReportPeriodAndDepartments_forNonexistent() {
        Notification result = notificationDao.findByReportPeriodAndDepartments(5, 2, 1);
        assertThat(result).isNull();
    }


    @Test
    public void test_deleteByReportPeriodAndDepartments() {
        Notification result = notificationDao.findByReportPeriodAndDepartments(1, 2, 1);
        assertThat(result).isNotNull();
        result = notificationDao.findByReportPeriodAndDepartments(1, 3, 1);
        assertThat(result).isNotNull();
        List<DepartmentPair> departments = Arrays.asList(
                new DepartmentPair(2, 1),
                new DepartmentPair(3, 1),
                new DepartmentPair(null, 1));
        notificationDao.deleteByReportPeriodAndDepartments(1, departments);
        result = notificationDao.findByReportPeriodAndDepartments(1, 2, 1);
        assertThat(result).isNull();
        result = notificationDao.findByReportPeriodAndDepartments(1, 3, 1);
        assertThat(result).isNull();
        result = notificationDao.findByReportPeriodAndDepartments(1, null, 1);
        assertThat(result).isNull();
    }


    @Test
    public void test_findByFilter_paging() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Collections.singletonList(1));

        assertThat(notificationDao.findByFilter(filter, null)).hasSize(3);
        assertThat(notificationDao.findByFilter(filter, PagingParams.getInstance(1, 2))).hasSize(2);
    }

    @Test
    public void test_findByFilter_byRead() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Collections.singletonList(1));
        filter.setRead(true);
        assertThat(notificationDao.findByFilter(filter, PagingParams.getInstance(1, 1000))).hasSize(2);
    }

    @Test
    public void test_findByFilter_byUserId() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertThat(notificationDao.findByFilter(userIdFilter, null)).hasSize(2);
    }

    @Test
    public void test_findByFilter_byUserRoles() {
        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertThat(notificationDao.findByFilter(userRoleFilter, null)).hasSize(3);
    }

    @Test
    public void test_findByFilter_byUserIdAndRoles() {
        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertThat(notificationDao.findByFilter(userAndRoleFilter, null)).hasSize(4);
    }


    @Test
    public void test_countByFilter() {
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setReceiverDepartmentIds(Collections.singletonList(1));
        filter.setRead(true);
        assertThat(notificationDao.countByFilter(filter)).isEqualTo(2);
        filter.setRead(null);
        assertThat(notificationDao.countByFilter(filter)).isEqualTo(3);

        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        assertThat(notificationDao.countByFilter(userIdFilter)).isEqualTo(2);

        NotificationsFilterData userRoleFilter = new NotificationsFilterData();
        userRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        assertThat(notificationDao.countByFilter(userRoleFilter)).isEqualTo(3);

        NotificationsFilterData userAndRoleFilter = new NotificationsFilterData();
        userAndRoleFilter.setUserRoleIds(Arrays.asList(2, 3));
        userAndRoleFilter.setUserId(3);
        assertThat(notificationDao.countByFilter(userAndRoleFilter)).isEqualTo(4);
    }


    @Test
    public void test_deleteByReportPeriod() {
        assertThat(notificationDao.findById(1)).isNotNull();
        notificationDao.deleteByReportPeriod(1);
        assertThat(notificationDao.findById(1)).isNull();
    }


    @Test
    public void test_setReadTrueByFilter() {
        NotificationsFilterData userIdFilter = new NotificationsFilterData();
        userIdFilter.setUserId(1);
        PagingParams paging = new PagingParams();

        List<Notification> notifications = notificationDao.findByFilter(userIdFilter, paging);
        assertThat(notifications.get(1).isRead()).isFalse();

        notificationDao.setReadTrueByFilter(userIdFilter);

        notifications = notificationDao.findByFilter(userIdFilter, paging);
        assertThat(notifications.get(1).isRead()).isTrue();
    }


    @Test
    public void test_deleteByIdIn() {
        assertThat(notificationDao.findById(1)).isNotNull();
        assertThat(notificationDao.findById(2)).isNotNull();

        notificationDao.deleteByIdIn(Arrays.asList(1L, 2L, 1234L));

        assertThat(notificationDao.findById(1)).isNull();
        assertThat(notificationDao.findById(2)).isNull();
    }


    @Test
    public void test_existsByUserIdAndReportId_byExistent() {
        boolean exists = notificationDao.existsByUserIdAndReportId(1, "uuid_2");
        assertThat(exists).isTrue();
    }

    @Test
    public void test_existsByUserIdAndReportId_byNonexistent() {
        boolean exists = notificationDao.existsByUserIdAndReportId(1, "uuid_3");
        assertThat(exists).isFalse();
    }
}
