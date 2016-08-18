package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.gwtplatform.dispatch.shared.Result;

public class TimerTaskResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    public enum FormMode {
        LOCKED, // Открыта только для чтения
        LOCKED_READ, // Заблокирована в режиме просмотра текущим пользователем
        LOCKED_EDIT, // Заблокирована в режиме редактрирования текущим пользователем
        EDIT, // Открыта для редактирования
        NOT_EXIT // Форма не существуует
    }

    private ReportType taskType;
    private FormMode formMode;
    private LockInfo lockInfo;
    private String taskName;
    private boolean edited;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
