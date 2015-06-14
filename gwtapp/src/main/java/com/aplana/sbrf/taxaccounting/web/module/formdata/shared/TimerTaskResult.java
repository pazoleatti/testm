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
    private String lockDate;
    private String lockedByUser;
    private String title;

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

    public String getLockDate() {
        return lockDate;
    }

    public void setLockDate(String lockDate) {
        this.lockDate = lockDate;
    }

    public String getLockedByUser() {
        return lockedByUser;
    }

    public void setLockedByUser(String lockedByUser) {
        this.lockedByUser = lockedByUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
