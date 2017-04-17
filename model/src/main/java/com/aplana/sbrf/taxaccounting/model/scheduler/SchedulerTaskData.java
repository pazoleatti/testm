package com.aplana.sbrf.taxaccounting.model.scheduler;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Информация о задаче планировщика
 * @author lhaziev
 */
public class SchedulerTaskData implements Serializable {
    private static final long serialVersionUID = 5615161290731046644L;

    /** Идентификатор задачи */
    private SchedulerTask task;

    /** Название задачи */
    private String taskName;

    /** Расписание задачи в формате CRON
     * second minute hourOfDay DayOfMonth Month DayOfWeek
     * Например 0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10
     * */
    private String schedule;

    /** Дата последей операции по редактированию параметров задачи */
    private Date modificationDate;

    /** Дата последнего запуска задачи */
    private Date last_fire_date;

    /**
     * Признак активности задачи
     */
    private boolean active;

    private List<SchedulerTaskParam> params;

    public SchedulerTask getTask() {
        return task;
    }

    public void setTask(SchedulerTask task) {
        this.task = task;
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

    public Date getLast_fire_date() {
        return last_fire_date;
    }

    public void setLast_fire_date(Date last_fire_date) {
        this.last_fire_date = last_fire_date;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SchedulerTaskParam> getParams() {
        return params;
    }

    public void setParams(List<SchedulerTaskParam> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TaskData{" +
                "task=" + task.name() +
                ", taskName='" + taskName + '\'' +
                ", schedule='" + schedule + '\'' +
                ", modificationDate=" + modificationDate +
                ", last_fire_date=" + last_fire_date +
                ", active=" + active +
                '}';
    }
}
