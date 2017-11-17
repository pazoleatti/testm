package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Класс для передачи SchedulerTaskData из ангуляра
 */
public class SchedulerTaskModel implements Serializable {

    /** Идентификатор задачи */
    private SchedulerTask task;

    /** Название задачи */
    private String taskName;

    /** Расписание задачи в формате CRON*/
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

    public SchedulerTaskData getShedulerTaskData( ){
        SchedulerTaskData schedulerTaskData =new SchedulerTaskData();
        schedulerTaskData.setTaskName(taskName);
        schedulerTaskData.setTask(task);
        schedulerTaskData.setSchedule(schedule);
        schedulerTaskData.setActive(active);
        schedulerTaskData.setParams(params);
        schedulerTaskData.setModificationDate(LocalDateTime.fromDateFields(modificationDate));
        schedulerTaskData.setLast_fire_date(LocalDateTime.fromDateFields(last_fire_date));
        return schedulerTaskData;
    }

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

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Date getLast_fire_date() {
        return last_fire_date;
    }

    public void setLast_fire_date(Date last_fire_date) {
        this.last_fire_date = last_fire_date;
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
}