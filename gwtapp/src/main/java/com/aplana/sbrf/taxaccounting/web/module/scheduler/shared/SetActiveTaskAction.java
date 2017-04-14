package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Возобновление задачи планировщика
 * @author dloshkarev
 */
public class SetActiveTaskAction extends UnsecuredActionImpl<SetActiveTaskResult> implements ActionName {

    private boolean active;
    private List<Long> tasksIds;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Long> getTasksIds() {
        return tasksIds;
    }

    public void setTasksIds(List<Long> tasksIds) {
        this.tasksIds = tasksIds;
    }

    @Override
    public String getName() {
        return "Возобновление задачи планировщика";
    }
}
