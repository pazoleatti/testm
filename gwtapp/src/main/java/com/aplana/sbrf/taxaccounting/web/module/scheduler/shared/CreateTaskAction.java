package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Создание новой задачи для планировщика
 * @author dloshkarev
 */
public class CreateTaskAction  extends UnsecuredActionImpl<CreateTaskResult> implements ActionName {

    /** Название задачи */
    private String taskName;

    /** Количество повторений задачи планировщиком */
    private int numberOfRepeats = -1;

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

    public int getNumberOfRepeats() {
        return numberOfRepeats;
    }

    public void setNumberOfRepeats(int numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
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
}
