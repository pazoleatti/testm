package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.Result;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


import java.util.List;

/**
 * Данные для задачи
 */
public abstract class TaskData<T extends Result> extends UnsecuredActionImpl<T> implements ActionName {
    /** Идентификатор задачи */
    private Long taskId;

    /** Название задачи */
    private String taskName;

    /** Расписание задачи в формате CRON
     * second minute hourOfDay DayOfMonth Month DayOfWeek
     * Например 0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10
     * */
    private String schedule;

    /** JNDI класса-обработчика задачи */
    private String userTaskJndi;

    /** Пользовательские параметры задачи */
    private List<TaskParamModel> params;

    @Override
    public String getName() {
        return "Создание задачи планировщика";
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getUserTaskJndi() {
        return userTaskJndi;
    }

    public void setUserTaskJndi(String userTaskJndi) {
        this.userTaskJndi = userTaskJndi;
    }

    public List<TaskParamModel> getParams() {
        return params;
    }

    public void setParams(List<TaskParamModel> params) {
        this.params = params;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
