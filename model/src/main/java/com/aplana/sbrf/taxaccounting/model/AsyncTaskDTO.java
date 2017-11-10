package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Класс для отображения данных асинхронных задач на клиенте
 * @author dloshkarev
 */
public class AsyncTaskDTO {
    private long id;
    /* Идентификатор пользователя, запустившего задачу*/
    private String user;
    /* Дата создания/помещения в очередь задачи */
    private Date createDate;
    /* Узел кластера (название машины), на котором выполняется задача */
    private String node;
    /* Описание задачи */
    private String description;
    /* Состояние задачи */
    private String state;
    /* Дата последнего изменения состояния задачи */
    private Date stateDate;
    /* Очередь, в которой находится связанная асинхронная задача */
    private String queue;
    /* Положение задачи в очереди */
    private int queuePosition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    @Override
    public String toString() {
        return "AsyncTaskDTO{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", createDate=" + createDate +
                ", node='" + node + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", stateDate=" + stateDate +
                ", queue=" + queue +
                ", queuePosition=" + queuePosition +
                '}';
    }
}
