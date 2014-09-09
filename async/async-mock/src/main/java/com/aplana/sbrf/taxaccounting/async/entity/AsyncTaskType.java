package com.aplana.sbrf.taxaccounting.async.entity;

/**
 * Спринговая реализация сущности с данными асинхронной задачи
 * @author dloshkarev
 */
public class AsyncTaskType {
    private long id;
    private String name;
    private String handlerJndi;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandlerJndi() {
        return handlerJndi;
    }

    public void setHandlerJndi(String handlerJndi) {
        this.handlerJndi = handlerJndi;
    }
}
