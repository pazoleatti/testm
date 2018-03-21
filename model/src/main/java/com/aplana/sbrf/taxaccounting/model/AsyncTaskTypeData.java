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
    private Long shortQueueLimit;
    /**
     * Ограничение на выполнение задачи
     */
    private Long taskLimit;

    /**
     * Наименование вида ограничения
     */
    private String limitKind;

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

    public Long getShortQueueLimit() {
        return shortQueueLimit;
    }

    public void setShortQueueLimit(Long shortQueueLimit) {
        this.shortQueueLimit = shortQueueLimit;
    }

    public Long getTaskLimit() {
        return taskLimit;
    }

    public void setTaskLimit(Long taskLimit) {
        this.taskLimit = taskLimit;
    }

    public String getLimitKind() {
        return limitKind;
    }

    public void setLimitKind(String limitKind) {
        this.limitKind = limitKind;
    }
}
