package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.gwtplatform.dispatch.shared.Result;

public class TimerTaskResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    public static enum FormMode {
        LOCKED, // Открыта только для чтения
        LOCKED_READ, // Заблокирована в режиме просмотра текущим пользователем
        LOCKED_EDIT, // Заблокирована в режиме редактрирования текущим пользователем
        EDIT // Открыта для редактирования
    }

    private ReportType taskType;
    private FormMode formMode;
    private LockInfo lockInfo;

    public ReportType getTaskType() {
        return taskType;
    }

    public void setTaskType(ReportType taskType) {
        this.taskType = taskType;
    }

    public FormMode getFormMode() {
        return formMode;
    }

    public void setFormMode(FormMode formMode) {
        this.formMode = formMode;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }
}
