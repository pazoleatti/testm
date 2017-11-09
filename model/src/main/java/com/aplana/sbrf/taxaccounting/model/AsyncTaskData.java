package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

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
    private LocalDateTime createDate;
    /* Описание задачи */
    private String description;
    /* Состояние задачи */
    private AsyncTaskState state;
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

    public Map<String, Object> getParams() {
        return params;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
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
                ", description='" + description + '\'' +
                ", state=" + state +
                ", createDate=" + createDate +
                ", params=" + params +
                '}';
    }
}
