package com.aplana.sbrf.taxaccounting.model.scheduler;

import java.io.Serializable;
import java.util.Date;

/**
 * Информация о задаче планировщика
 * @author lhaziev
 */
public class SchedulerTaskParam implements Serializable {
    private static final long serialVersionUID = 5615161290731046644L;

    private long id;

    /**
     * Название задачи
     */
    private String paramName;

    /**
     * Тип параметра
     */
    private SchedulerTaskParamType paramType;

    /**
     * Значение
     */
    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SchedulerTaskParamType getParamType() {
        return paramType;
    }

    public void setParamType(SchedulerTaskParamType paramType) {
        this.paramType = paramType;
    }
}
