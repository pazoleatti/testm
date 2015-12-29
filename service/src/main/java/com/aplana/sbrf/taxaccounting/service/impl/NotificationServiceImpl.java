package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Override
    public Notification get(long id) {
        return notificationDao.get(id);
    }

    @Override
    public long save(Notification notification) {
        return notificationDao.save(notification);
    }

    @Override
    public void saveList(List<Notification> notifications) {
        if (notifications.get(0).getReportPeriodId() != null) {
            //Выполняется сохранение уведомлений по сроку сдачи отчетности
            List<DepartmentPair> departments = new ArrayList<DepartmentPair>();
            for (Notification item : notifications) {
                departments.add(new DepartmentPair(item.getSenderDepartmentId(), item.getReceiverDepartmentId()));
            }
            notificationDao.deleteList(notifications.get(0).getReportPeriodId(), departments);
        }
        notificationDao.saveList(notifications);
    }

    @Override
    public Notification get(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId) {
        return notificationDao.get(reportPeriodId, senderDepartmentId, receiverDepartmentId);
    }

    @Override
    public Map<Integer, Notification> mapByDepartments(int senderDepartmentId, Integer receiverDepartmentId) {
        Map<Integer, Notification> notificationMap = new HashMap<Integer, Notification>();
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setSenderDepartmentId(senderDepartmentId);
        filter.setReceiverDepartmentIds(Arrays.asList(receiverDepartmentId));
        List<Notification> list = notificationDao.getByFilter(filter);
        for (Notification notification : list) {
            notificationMap.put(notification.getReportPeriodId(), notification);
        }
        return notificationMap;
    }

	@Override
	public PagingResult<Notification> getByFilter(NotificationsFilterData filter) {
		List<Notification> notifications = notificationDao.getByFilter(filter);
		return new PagingResult<Notification>(notifications, getCountByFilter(filter));
	}

	@Override
	public int getCountByFilter(NotificationsFilterData filter) {
		return notificationDao.getCountByFilter(filter);
	}

    @Override
    public void deleteByReportPeriod(int reportPeriodId) {
        notificationDao.deleteByReportPeriod(reportPeriodId);
    }

    @Override
    public void updateUserNotificationsStatus(NotificationsFilterData filter) {
        notificationDao.updateUserNotificationsStatus(filter);
    }

    @Override
    public void deleteAll(List<Long> notificationIds) {
        notificationDao.deleteAll(notificationIds);
    }
}
