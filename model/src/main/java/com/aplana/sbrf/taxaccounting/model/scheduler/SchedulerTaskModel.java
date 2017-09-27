package com.aplana.sbrf.taxaccounting.model.scheduler;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

/**
 * Информация о задаче планировщика (с признаком активности типа Byte)
 */
public class SchedulerTaskModel implements Serializable{
    private static final long serialVersionUID = 8499843706617670293L;
    /** Идентификатор задачи */
    private int id;

    /** Название задачи */
    private String taskName;

    /** Расписание задачи в формате CRON
     * second minute hourOfDay DayOfMonth Month DayOfWeek
     * Например 0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10
     * */
    private String schedule;

    /** Дата последей операции по редактированию параметров задачи */
    private LocalDateTime modificationDate;

    /** Дата последнего запуска задачи */
    private LocalDateTime lastFireDate;

    /**
     * Признак активности задачи
     */
    private Byte active;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public LocalDateTime getLastFireDate() {
        return lastFireDate;
    }

    public void setLastFireDate(LocalDateTime lastFireDate) {
        this.lastFireDate = lastFireDate;
    }

    public Byte getActive() {
        return active;
    }

    public void setActive(Byte active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "TaskModel{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", schedule='" + schedule + '\'' +
                ", modificationDate=" + modificationDate +
                ", lastFireDate=" + lastFireDate +
                ", active=" + active +
                '}';
    }
}
