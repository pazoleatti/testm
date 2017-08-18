package com.aplana.sbrf.taxaccounting.model;


/**
 * Модель для работы с типами асинхронных задач
 */
public class AsyncTaskTypeData {

    /**
     * Идентификатор типа задачи
     */
    private long id;
    /**
     * Имя типа задачи
     */
    private String name;
    /**
     * Класс-обработчик задачи
     */
    private String handlerClassName;
    /**
     * Ограничение на выполнение задачи в очереди быстрых задач
     */
    private long shortQueueLimit;
    /**
     * Ограничение на выполнение задачи
     */
    private long taskLimit;

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

    public String getHandlerClassName() {
        return handlerClassName;
    }

    public void setHandlerClassName(String handlerClassName) {
        this.handlerClassName = handlerClassName;
    }

    public long getShortQueueLimit() {
        return shortQueueLimit;
    }

    public void setShortQueueLimit(long shortQueueLimit) {
        this.shortQueueLimit = shortQueueLimit;
    }

    public long getTaskLimit() {
        return taskLimit;
    }

    public void setTaskLimit(long taskLimit) {
        this.taskLimit = taskLimit;
    }
}
