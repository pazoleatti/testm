package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

/**
 * Обновление задачи планировщика
 */
public class UpdateTaskAction extends TaskData<UpdateTaskResult> {
    private Long contextId;

    @Override
    public String getName() {
        return "Обновление задачи планировщика";
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
}
