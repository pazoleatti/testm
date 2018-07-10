package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private BlobDataService blobDataService;

    @Override
    public Notification fetchOne(long id) {
        return notificationDao.fetchOne(id);
    }

    @Override
    public void create(List<Notification> notifications) {
        if (notifications.get(0).getReportPeriodId() != null) {
            //Выполняется сохранение уведомлений по сроку сдачи отчетности
            List<DepartmentPair> departments = new ArrayList<DepartmentPair>();
            for (Notification item : notifications) {
                departments.add(new DepartmentPair(item.getSenderDepartmentId(), item.getReceiverDepartmentId()));
            }
            notificationDao.delete(notifications.get(0).getReportPeriodId(), departments);
        }
        notificationDao.create(notifications);
    }

    @Override
    public Notification fetchOne(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId) {
        return notificationDao.fetchOne(reportPeriodId, senderDepartmentId, receiverDepartmentId);
    }

    @Override
    public PagingResult<Notification> fetchByFilter(NotificationsFilterData filter) {
        List<Notification> notifications = notificationDao.fetchAllByFilter(filter);
        return new PagingResult<>(notifications, fetchCountByFilter(filter));
    }

    @Override
    public PagingResult<Notification> fetchAllByFilterAndPaging(NotificationsFilterData filter, PagingParams pagingParams) {
        List<Notification> notifications = notificationDao.fetchAllByFilterAndPaging(filter, pagingParams);
        return new PagingResult<>(notifications, fetchCountByFilter(filter));
    }

    @Override
    public int fetchCountByFilter(NotificationsFilterData filter) {
        return notificationDao.fetchCountByFilter(filter);
    }

    @Override
    public void deleteByReportPeriod(int reportPeriodId) {
        notificationDao.deleteByReportPeriod(reportPeriodId);
    }

    @Override
    public void updateReadTrueByFilter(NotificationsFilterData filter) {
        notificationDao.updateReadTrueByFilter(filter);
    }

    @Override
    public void deleteAll(List<Long> notificationIds) {
        notificationDao.deleteAll(notificationIds);
    }

    @Override
    public Date fetchLastNotificationDate() {
        return notificationDao.fetchLastNotificationDate();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public BlobData getNotificationBlobData(String blobDataId) {
        return blobDataService.get(blobDataId);
    }
}
