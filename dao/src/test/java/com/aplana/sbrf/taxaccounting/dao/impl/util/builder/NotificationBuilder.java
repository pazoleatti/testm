package com.aplana.sbrf.taxaccounting.dao.impl.util.builder;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationType;

import java.util.Date;

public final class NotificationBuilder {
    private Notification notification;

    private NotificationBuilder() {
        notification = new Notification();
    }

    public static NotificationBuilder aNotification() {
        return new NotificationBuilder();
    }

    public NotificationBuilder id(Long id) {
        notification.setId(id);
        return this;
    }

    public NotificationBuilder reportPeriodId(Integer reportPeriodId) {
        notification.setReportPeriodId(reportPeriodId);
        return this;
    }

    public NotificationBuilder senderDepartmentId(Integer senderDepartmentId) {
        notification.setSenderDepartmentId(senderDepartmentId);
        return this;
    }

    public NotificationBuilder receiverDepartmentId(Integer receiverDepartmentId) {
        notification.setReceiverDepartmentId(receiverDepartmentId);
        return this;
    }

    public NotificationBuilder text(String text) {
        notification.setText(text);
        return this;
    }

    public NotificationBuilder logId(String logId) {
        notification.setLogId(logId);
        return this;
    }

    public NotificationBuilder createDate(Date createDate) {
        notification.setCreateDate(createDate);
        return this;
    }

    public NotificationBuilder deadline(Date deadline) {
        notification.setDeadline(deadline);
        return this;
    }

    public NotificationBuilder userId(Integer userId) {
        notification.setUserId(userId);
        return this;
    }

    public NotificationBuilder roleId(Integer roleId) {
        notification.setRoleId(roleId);
        return this;
    }

    public NotificationBuilder read(boolean read) {
        notification.setRead(read);
        return this;
    }

    public NotificationBuilder reportId(String reportId) {
        notification.setReportId(reportId);
        return this;
    }

    public NotificationBuilder notificationType(NotificationType notificationType) {
        notification.setNotificationType(notificationType);
        return this;
    }

    public NotificationBuilder but() {
        return aNotification().id(notification.getId()).reportPeriodId(notification.getReportPeriodId()).senderDepartmentId(notification.getSenderDepartmentId()).receiverDepartmentId(notification.getReceiverDepartmentId()).text(notification.getText()).logId(notification.getLogId()).createDate(notification.getCreateDate()).deadline(notification.getDeadline()).userId(notification.getUserId()).roleId(notification.getRoleId()).read(notification.isRead()).reportId(notification.getReportId()).notificationType(notification.getNotificationType());
    }

    public Notification build() {
        return notification;
    }
}
