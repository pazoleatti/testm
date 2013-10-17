package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Возобновление задачи планировщика
 * @author dloshkarev
 */
public class ResumeTaskAction  extends UnsecuredActionImpl<ResumeTaskResult> implements ActionName {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return "Возобновление задачи планировщика";
    }
}
