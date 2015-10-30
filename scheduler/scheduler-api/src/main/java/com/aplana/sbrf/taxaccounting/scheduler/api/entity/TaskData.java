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

    /** Дата последей операции по редактированию параметров задачи */
    private Date modificationDate;

    /** Дата создания задачи */
    private Date timeCreated;

    /** Дата следующего запуска задачи */
    private Date nextFireTime;

    /** Пользовательские параметры задачи */
    private Map<String, TaskParam> params;

    /** Идентификатор контекста задачи */
    private Long contextId;

    /** Задача была признана устаревшей и теперь удалена */
    private boolean oldAndDeleted;

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

    public boolean isOldAndDeleted() {
        return oldAndDeleted;
    }

    public void setOldAndDeleted(boolean oldAndDeleted) {
        this.oldAndDeleted = oldAndDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskData taskData = (TaskData) o;

        if (numberOfRepeats != taskData.numberOfRepeats) return false;
        if (oldAndDeleted != taskData.oldAndDeleted) return false;
        if (contextId != null ? !contextId.equals(taskData.contextId) : taskData.contextId != null) return false;
        if (modificationDate != null ? !modificationDate.equals(taskData.modificationDate) : taskData.modificationDate != null)
            return false;
        if (nextFireTime != null ? !nextFireTime.equals(taskData.nextFireTime) : taskData.nextFireTime != null)
            return false;
        if (params != null ? !params.equals(taskData.params) : taskData.params != null) return false;
        if (repeatsLeft != null ? !repeatsLeft.equals(taskData.repeatsLeft) : taskData.repeatsLeft != null)
            return false;
        if (schedule != null ? !schedule.equals(taskData.schedule) : taskData.schedule != null) return false;
        if (taskId != null ? !taskId.equals(taskData.taskId) : taskData.taskId != null) return false;
        if (taskName != null ? !taskName.equals(taskData.taskName) : taskData.taskName != null) return false;
        if (taskState != taskData.taskState) return false;
        if (timeCreated != null ? !timeCreated.equals(taskData.timeCreated) : taskData.timeCreated != null)
            return false;
        if (userTaskJndi != null ? !userTaskJndi.equals(taskData.userTaskJndi) : taskData.userTaskJndi != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = taskId != null ? taskId.hashCode() : 0;
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        result = 31 * result + (taskState != null ? taskState.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (userTaskJndi != null ? userTaskJndi.hashCode() : 0);
        result = 31 * result + numberOfRepeats;
        result = 31 * result + (repeatsLeft != null ? repeatsLeft.hashCode() : 0);
        result = 31 * result + (modificationDate != null ? modificationDate.hashCode() : 0);
        result = 31 * result + (timeCreated != null ? timeCreated.hashCode() : 0);
        result = 31 * result + (nextFireTime != null ? nextFireTime.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (contextId != null ? contextId.hashCode() : 0);
        result = 31 * result + (oldAndDeleted ? 1 : 0);
        return result;
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
                ", modificationDate=" + modificationDate +
                ", timeCreated=" + timeCreated +
                ", nextFireTime=" + nextFireTime +
                ", params=" + params +
                ", contextId=" + contextId +
                ", oldAndDeleted=" + oldAndDeleted +
                '}';
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public Long getContextId() {
        return contextId;
    }
}
