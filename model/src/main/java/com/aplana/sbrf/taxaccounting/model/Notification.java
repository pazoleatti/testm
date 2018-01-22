package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Objects;

/**
 * Оповещения о назначении даты сдачи отчетности
 *
 * @author dloshkarev
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = -5255606476850599691L;

    private static final int MAX_LENGTH_TEXT = 1000;

    private Long id;
    private Integer reportPeriodId;
    private Integer senderDepartmentId;
    private Integer receiverDepartmentId;
    private String text;
    private String logId;
    private LocalDateTime createDate;
    private LocalDateTime deadline;
    /**
     * Идентификатор пользователя, который получит оповещение
     */
    private Integer userId;
    /**
     * Идентификатор роли пользователя, который получит оповещение
     */
    private Integer roleId;
    /**
     * Признак прочтения
     */
    private boolean read;
    /**
     * Идентификатор отчета
     */
    private String reportId;
    /**
     * Тип оповещения
     */
    private NotificationType notificationType = NotificationType.DEFAULT;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getSenderDepartmentId() {
        return senderDepartmentId;
    }

    public void setSenderDepartmentId(Integer senderDepartmentId) {
        this.senderDepartmentId = senderDepartmentId;
    }

    public Integer getReceiverDepartmentId() {
        return receiverDepartmentId;
    }

    public void setReceiverDepartmentId(Integer receiverDepartmentId) {
        this.receiverDepartmentId = receiverDepartmentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        text = text.replaceAll("\"\"+", "\"");
        if (text != null && !text.isEmpty() && text.length() > MAX_LENGTH_TEXT) {
            this.text = text.substring(0, MAX_LENGTH_TEXT - 3) + "...";
        } else {
            this.text = text;
        }
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return read == that.read &&
                Objects.equals(id, that.id) &&
                Objects.equals(reportPeriodId, that.reportPeriodId) &&
                Objects.equals(senderDepartmentId, that.senderDepartmentId) &&
                Objects.equals(receiverDepartmentId, that.receiverDepartmentId) &&
                Objects.equals(text, that.text) &&
                Objects.equals(logId, that.logId) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(deadline, that.deadline) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(reportId, that.reportId) &&
                notificationType == that.notificationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportPeriodId, senderDepartmentId, receiverDepartmentId, text, logId, createDate, deadline, userId, roleId, read, reportId, notificationType);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", reportPeriodId=" + reportPeriodId +
                ", senderDepartmentId=" + senderDepartmentId +
                ", receiverDepartmentId=" + receiverDepartmentId +
                ", text='" + text + '\'' +
                ", logId='" + logId + '\'' +
                ", createDate=" + createDate +
                ", deadline=" + deadline +
                ", userId=" + userId +
                ", roleId=" + roleId +
                ", read=" + read +
                ", reportId='" + reportId + '\'' +
                ", notificationType=" + notificationType +
                '}';
    }
}
