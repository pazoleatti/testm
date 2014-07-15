package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Остановка задачи планировщика
 * @author dloshkarev
 */
public class StopTaskAction extends UnsecuredActionImpl<StopTaskResult> implements ActionName {

    private List<Long> tasksIds;

    public List<Long> getTasksIds() {
        return tasksIds;
    }

    public void setTasksIds(List<Long> tasksIds) {
        this.tasksIds = tasksIds;
    }

    @Override
    public String getName() {
        return "Остановка задачи планировщика";
    }
}
