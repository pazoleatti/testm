package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Запуск задачи планировщика
 * @author dloshkarev
 */
public class StartTaskAction extends UnsecuredActionImpl<StartTaskResult> implements ActionName {

    private List<Long> tasksIds;

    @Override
    public String getName() {
        return "Запуск задачи планировщика";
    }

    public List<Long> getTasksIds() {
        return tasksIds;
    }

    public void setTasksIds(List<Long> tasksIds) {
        this.tasksIds = tasksIds;
    }
}
