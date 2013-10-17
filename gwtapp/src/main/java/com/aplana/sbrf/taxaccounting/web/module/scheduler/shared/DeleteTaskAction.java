package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Удаление задачи планировщика
 * @author dloshkarev
 */
public class DeleteTaskAction  extends UnsecuredActionImpl<DeleteTaskResult> implements ActionName {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return "Удаление задачи планировщика";
    }
}
