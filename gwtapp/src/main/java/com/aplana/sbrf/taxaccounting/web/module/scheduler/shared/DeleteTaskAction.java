package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Удаление задачи планировщика
 * @author dloshkarev
 */
public class DeleteTaskAction  extends UnsecuredActionImpl<DeleteTaskResult> implements ActionName {

    private List<Long> tasksIds;

    public void setTasksIds(List<Long> tasksIds) {
        this.tasksIds = tasksIds;
    }

    @Override
    public String getName() {
        return "Удаление задачи планировщика";
    }

    public List<Long> getTasksIds() {
        return tasksIds;
    }
}
