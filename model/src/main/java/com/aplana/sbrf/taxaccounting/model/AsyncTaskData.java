package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Данные конкретной асинхронной задачи
 *
 * @author dloshkarev
 */
public class AsyncTaskData {
    private long id;
    /* Тип задачи - хранит класс-исполнитель задачи */
    private AsyncTaskType type;
    /* Идентификатор пользователя, запустившего задачу*/
    private int userId;
    /* Дата создания/помещения в очередь задачи */
    private Date createDate;
    /* Узел кластера (название машины), на котором выполняется задача */
    private String node;
    /* Описание задачи */
    private String description;
    /* Состояние задачи */
    private AsyncTaskState state;
    /* Дата последнего изменения состояния задачи */
    private Date stateDate;
    /* Очередь, в которой находится связанная асинхронная задача */
    private AsyncQueue queue;
    /* Положение задачи в очереди */
    private int queuePosition;
    /**
     * Параметры для выполнения конкретной задачи
     */
    private Map<String, Object> params = new HashMap<String, Object>(0);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AsyncTaskType getType() {
        return type;
    }

    public void setType(AsyncTaskType type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public AsyncTaskState getState() {
        return state;
    }

    public void setState(AsyncTaskState state) {
        this.state = state;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public AsyncQueue getQueue() {
        return queue;
    }

    public void setQueue(AsyncQueue queue) {
        this.queue = queue;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "AsyncTaskData{" +
                "id=" + id +
                ", type=" + type +
                ", userId=" + userId +
                ", createDate=" + createDate +
                ", node='" + node + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", stateDate=" + stateDate +
                ", queue=" + queue +
                ", queuePosition=" + queuePosition +
                ", params=" + params +
                '}';
    }
}
