package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Результат получения списка задач планировщика
 * @author dloshkarev
 */
public class GetTaskListResult implements Result {
    private static final long serialVersionUID = -6724286641397890369L;

    /**
     * Список задач планировщика
     */
    private List<TaskSearchResultItem> tasks;
    private String uuid;

    public List<TaskSearchResultItem> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSearchResultItem> tasks) {
        this.tasks = tasks;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
