package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Запуск задачи планировщика
 * @author dloshkarev
 */
public class StartTaskAction extends UnsecuredActionImpl<StartTaskResult> implements ActionName {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return "Запуск задачи планировщика";
    }
}
