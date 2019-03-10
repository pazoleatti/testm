package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Оповещения о назначении даты сдачи отчетности
 */
@Getter
@Setter
public class Notification implements SecuredEntity, Serializable {
    private static final long serialVersionUID = -5255606476850599691L;

    private static final int MAX_LENGTH_TEXT = 1000;

    private Long id;
    private Integer reportPeriodId;
    private Integer senderDepartmentId;
    private Integer receiverDepartmentId;
    private String text;
    private String logId;
    private Date createDate;
    private Date deadline;
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

    /**
     * Права доступа
     */
    private long permissions;


    public void setText(String text) {
        text = text.replaceAll("\"\"+", "\"");
        if (text != null && !text.isEmpty() && text.length() > MAX_LENGTH_TEXT) {
            this.text = text.substring(0, MAX_LENGTH_TEXT - 3) + "...";
        } else {
            this.text = text;
        }
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
