package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Контекст задачи, выполняемой в планировщике. Содержит данные, для выполнения логики задачи
 * @author dloshkarev
 */
public class TaskContext implements Serializable {
    private static final long serialVersionUID = -1981721618349446095L;

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
    private Map<String, TaskParam> params = new HashMap<String, TaskParam>(0);

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

    public Map<String, TaskParam> getParams() {
        return params;
    }

    public void setParams(Map<String, TaskParam> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TaskContext{" +
                "taskName='" + taskName + '\'' +
                ", numberOfRepeats=" + numberOfRepeats +
                ", schedule='" + schedule + '\'' +
                ", userTaskJndi='" + userTaskJndi + '\'' +
                ", params=" + params +
                '}';
    }
}
