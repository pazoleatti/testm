package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;

/**
 * Результат получения информации о задаче
 * @author dloshkarev
 */
public class GetTaskInfoResult implements Result {
    private static final long serialVersionUID = -6048797889536578398L;

    /** Идентификатор задачи */
    private Long taskId;

    /** Идентификатор контекста задачи */
    private Long contextId;

    /** Название задачи */
    private String taskName;

    /** Состояние задачи */
    private Integer taskState;

    /** Расписание задачи в формате CRON
     * second minute hourOfDay DayOfMonth Month DayOfWeek
     * Например 0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10
     * */
    private String schedule;

    /** JNDI класса-обработчика задачи */
    private String userTaskJndi;

    /** Количество выполненных повторений задачи */
    private Integer repeatsLeft;

    /** Дата создания задачи */
    private Date timeCreated;

    /** Дата следующего запуска задачи */
    private Date nextFireTime;

    /** Пользовательские параметры задачи */
    private List<SchedulerTaskParam> params;

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

    public Integer getTaskState() {
        return taskState;
    }

    public void setTaskState(Integer taskState) {
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

    public List<SchedulerTaskParam> getParams() {
        return params;
    }

    public void setParams(List<SchedulerTaskParam> params) {
        this.params = params;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

}
