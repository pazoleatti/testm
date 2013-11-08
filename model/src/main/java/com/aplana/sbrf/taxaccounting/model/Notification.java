package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Оповещения о назначении даты сдачи отчетности
 * @author dloshkarev
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = -5255606476850599691L;

    private int id;
    private Integer reportPeriodId;
    private Integer senderDepartmentId;
    private Integer receiverDepartmentId;
    private int firstReaderId;
    private String text;
    private Date createDate;
    private Date deadline;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getFirstReaderId() {
        return firstReaderId;
    }

    public void setFirstReaderId(int firstReaderId) {
        this.firstReaderId = firstReaderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", reportPeriodId=" + reportPeriodId +
                ", senderDepartmentId=" + senderDepartmentId +
                ", receiverDepartmentId=" + receiverDepartmentId +
                ", firstReaderId=" + firstReaderId +
                ", text='" + text + '\'' +
                ", createDate=" + createDate +
                ", deadline=" + deadline +
                '}';
    }
}
