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
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private BlobDataService blobDataService;

    @Override
    public void create(List<Notification> notifications) {
        if (notifications.get(0).getReportPeriodId() != null) {
            //Выполняется сохранение уведомлений по сроку сдачи отчетности
            List<DepartmentPair> departments = new ArrayList<>();
            for (Notification item : notifications) {
                departments.add(new DepartmentPair(item.getSenderDepartmentId(), item.getReceiverDepartmentId()));
            }
            notificationDao.deleteByReportPeriodAndDepartments(notifications.get(0).getReportPeriodId(), departments);
        }
        notificationDao.create(notifications);
    }

    @Override
    public List<Notification> findByIdIn(List<Long> ids) {
        return notificationDao.findByIdIn(ids);
    }

    @Override
    public PagingResult<Notification> findByFilter(NotificationsFilterData filter, PagingParams pagingParams) {
        List<Notification> notifications = notificationDao.findByFilter(filter, pagingParams);
        return new PagingResult<>(notifications, notificationDao.countByFilter(filter));
    }

    @Override
    public int countByFilter(NotificationsFilterData filter) {
        return notificationDao.countByFilter(filter);
    }

    @Override
    public void deleteByReportPeriod(int reportPeriodId) {
        notificationDao.deleteByReportPeriod(reportPeriodId);
    }

    @Override
    public void setReadTrueByFilter(NotificationsFilterData filter) {
        notificationDao.setReadTrueByFilter(filter);
    }

    @Override
    public void deleteByIdIn(List<Long> notificationIds) {
        notificationDao.deleteByIdIn(notificationIds);
    }

    @Override
    @PreAuthorize("hasPermission(#notification, T(com.aplana.sbrf.taxaccounting.permissions.NotificationPermission).DOWNLOAD_NOTIFICATION_FILE)")
    public BlobData getNotificationBlobData(Notification notification) {
        return blobDataService.get(notification.getReportId());
    }
}
