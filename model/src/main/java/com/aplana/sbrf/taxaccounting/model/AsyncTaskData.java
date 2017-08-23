package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Данные конкретной асинхронной задачи
 * @author dloshkarev
 */
public class AsyncTaskData {
    private Long id;
    /**
     * Тип задачи - хранит класс-исполнитель задачи
     */
    private Long typeId;
    /**
     * Дата создания/помещения в очередь задачи
     */
    private Date createDate;
    /**
     * Узел кластера (название машины), на который назначена задача
     */
    private String node;
    /**
     * Параметры для выполнения конкретной задачи
     */
    private Map<String, Object> params = new HashMap<String, Object>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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
                ", typeId=" + typeId +
                ", createDate=" + createDate +
                ", node='" + node + '\'' +
                ", params=" + params +
                '}';
    }
}
