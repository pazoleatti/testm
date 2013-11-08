package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Override
    public int save(Notification notification) {
        return notificationDao.save(notification);
    }

    @Override
    public void saveList(List<Notification> notifications) {
        List<DepartmentPair> departments = new ArrayList<DepartmentPair>();
        for (Notification item : notifications) {
            departments.add(new DepartmentPair(item.getSenderDepartmentId(), item.getReceiverDepartmentId()));
        }
        notificationDao.deleteList(notifications.get(0).getReportPeriodId(), departments);
        notificationDao.saveList(notifications);
    }

    @Override
    public Notification get(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId) {
        return notificationDao.get(reportPeriodId, senderDepartmentId, receiverDepartmentId);
    }

    @Override
    public Map<Integer, Notification> mapByDepartments(int senderDepartmentId, Integer receiverDepartmentId) {
        Map<Integer, Notification> notificationMap = new HashMap<Integer, Notification>();
        List<Notification> list = notificationDao.listByDepartments(senderDepartmentId, receiverDepartmentId);
        for (Notification notification : list) {
            notificationMap.put(notification.getReportPeriodId(), notification);
        }
        return notificationMap;
    }
}
