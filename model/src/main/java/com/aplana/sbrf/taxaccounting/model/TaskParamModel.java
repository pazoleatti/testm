package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель пользовательских параметров задачи
 * @author dloshkarev
 */
public class TaskParamModel implements Serializable {
    private static final long serialVersionUID = -4785538106276474385L;

    /**
     * Название параметра
     */
    private String taskParamName;

    /**
     * Тип параметра
     */
    private Integer taskParamType;

    /**
     * Значение параметра
     */
    private String taskParamValue = null;

    /**
     * Используется, если значение параметра - дата
     */
    private Date taskParamDateValue = null;

    public String getTaskParamName() {
        return taskParamName;
    }

    public void setTaskParamName(String taskParamName) {
        this.taskParamName = taskParamName;
    }

    public Integer getTaskParamType() {
        return taskParamType;
    }

    public void setTaskParamType(Integer taskParamType) {
        this.taskParamType = taskParamType;
    }

    public String getTaskParamValue() {
        return taskParamValue;
    }

    public void setTaskParamValue(String taskParamValue) {
        this.taskParamValue = taskParamValue;
    }

    public Date getTaskParamDateValue() {
        return taskParamDateValue;
    }

    public void setTaskParamDateValue(Date taskParamDateValue) {
        this.taskParamDateValue = taskParamDateValue;
    }
}
