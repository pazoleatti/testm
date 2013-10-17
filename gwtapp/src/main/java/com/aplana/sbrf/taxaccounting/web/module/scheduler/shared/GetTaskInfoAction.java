package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение информации о задаче планировщика
 * @author dloshkarev
 */
public class GetTaskInfoAction extends UnsecuredActionImpl<GetTaskInfoResult> implements ActionName {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getName() {
        return "Получение информации о задаче планировщика";
    }
}
