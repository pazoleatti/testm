package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Остановка задачи планировщика
 * @author dloshkarev
 */
public class StopTaskAction extends UnsecuredActionImpl<StopTaskResult> implements ActionName {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return "Остановка задачи планировщика";
    }
}
