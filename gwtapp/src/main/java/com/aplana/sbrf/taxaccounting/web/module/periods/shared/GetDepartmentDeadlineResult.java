package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;

/**
 * Результат получения срока сдачи отчетности
 * @author dloshkarev
 */
public class GetDepartmentDeadlineResult implements Result {
    private static final long serialVersionUID = -1563267157516809600L;

    private Date deadline;

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
