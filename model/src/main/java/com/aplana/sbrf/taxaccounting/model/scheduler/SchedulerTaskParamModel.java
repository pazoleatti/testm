package com.aplana.sbrf.taxaccounting.model.scheduler;

import java.io.Serializable;

/**
 * Информация о задаче планировщика(с полем type типа Byte)
 */
public class SchedulerTaskParamModel implements Serializable {
    private static final long serialVersionUID = -2161977540377185756L;

    private int id;

    /**
     * Название задачи
     */
    private String paramName;

    /**
     * Тип параметра
     */
    private Byte type;

    /**
     * Значение
     */
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }
}
