package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.gwtplatform.dispatch.shared.Result;

public class TimerTaskResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private ReportType taskType;

    public ReportType getTaskType() {
        return taskType;
    }

    public void setTaskType(ReportType taskType) {
        this.taskType = taskType;
    }
}
