package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Информация о задаче планировщика
 * @author dloshkarev
 */
public class TaskData implements Serializable {
    private static final long serialVersionUID = 6010564232594538420L;

    /** Идентификатор задачи */
    private Long taskId;

    /** Название задачи */
    private String taskName;

    /** Состояние задачи */
    private TaskState taskState;

    /** Расписание задачи в формате CRON
     * second minute hourOfDay DayOfMonth Month DayOfWeek
     * Например 0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10
     * */
    private String schedule;

    /** JNDI класса-обработчика задачи */
    private String userTaskJndi;

    /** Количество повторений задачи планировщиком */
    private int numberOfRepeats;

    /** Количество выполненных повторений задачи */
    private Integer repeatsLeft;

    /** Дата создания задачи */
    private Date timeCreated;

    /** Дата следующего запуска задачи */
    private Date nextFireTime;

    /** Пользовательские параметры задачи */
    private Map<String, TaskParam> params;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
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

    public int getNumberOfRepeats() {
        return numberOfRepeats;
    }

    public void setNumberOfRepeats(int numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
    }

    public Integer getRepeatsLeft() {
        return repeatsLeft;
    }

    public void setRepeatsLeft(Integer repeatsLeft) {
        this.repeatsLeft = repeatsLeft;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Map<String, TaskParam> getParams() {
        return params;
    }

    public void setParams(Map<String, TaskParam> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TaskData{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", taskState=" + taskState +
                ", schedule='" + schedule + '\'' +
                ", userTaskJndi='" + userTaskJndi + '\'' +
                ", numberOfRepeats=" + numberOfRepeats +
                ", repeatsLeft=" + repeatsLeft +
                ", timeCreated=" + timeCreated +
                ", nextFireTime=" + nextFireTime +
                ", params=" + params +
                '}';
    }
}
