package com.aplana.sbrf.taxaccounting.scheduler.api.form;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;

import java.io.Serializable;

/**
 * Абстрактный класс представляющий из себя элемент формы
 */
public abstract class FormElement implements Serializable {
    private TaskParamType type;
    private String name;
    private boolean required;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskParamType getType() {
        return type;
    }

    public void setType(TaskParamType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
