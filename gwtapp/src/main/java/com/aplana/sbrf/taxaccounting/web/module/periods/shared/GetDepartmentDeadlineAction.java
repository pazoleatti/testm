package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение срока сдачи отчетности для подразделения
 * @author dloshkarev
 */
public class GetDepartmentDeadlineAction extends UnsecuredActionImpl<GetDepartmentDeadlineResult> {
    private int reportPeriodId;
    private int senderDepartmentId;
    private Integer receiverDepartmentId;

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public int getSenderDepartmentId() {
        return senderDepartmentId;
    }

    public void setSenderDepartmentId(int senderDepartmentId) {
        this.senderDepartmentId = senderDepartmentId;
    }

    public Integer getReceiverDepartmentId() {
        return receiverDepartmentId;
    }

    public void setReceiverDepartmentId(Integer receiverDepartmentId) {
        this.receiverDepartmentId = receiverDepartmentId;
    }
}
