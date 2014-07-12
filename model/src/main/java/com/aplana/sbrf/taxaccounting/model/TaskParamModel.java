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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskParamModel model = (TaskParamModel) o;

        if (taskParamName != null ? !taskParamName.equals(model.taskParamName) : model.taskParamName != null)
            return false;
        if (taskParamType != null ? !taskParamType.equals(model.taskParamType) : model.taskParamType != null)
            return false;
        if (taskParamValue != null ? !taskParamValue.equals(model.taskParamValue) : model.taskParamValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = taskParamName != null ? taskParamName.hashCode() : 0;
        result = 31 * result + (taskParamType != null ? taskParamType.hashCode() : 0);
        result = 31 * result + (taskParamValue != null ? taskParamValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskParamModel{" +
                "taskParamName='" + taskParamName + '\'' +
                ", taskParamType=" + taskParamType +
                ", taskParamValue='" + taskParamValue + '\'' +
                '}';
    }
}
