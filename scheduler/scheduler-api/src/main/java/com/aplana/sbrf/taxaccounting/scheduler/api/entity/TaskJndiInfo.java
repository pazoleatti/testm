package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import java.io.Serializable;
import java.util.List;

/**
 * Информация по jndi-именам задач
 * @author dloshkarev
 */
public class TaskJndiInfo implements Serializable {
    private static final long serialVersionUID = 348462594878912721L;

    /** Название задачи */
    private String name;

    /** JNDI-имя задачи */
    private String jndi;

    private List<FormElement> params;

    public TaskJndiInfo(String name, String jndi, List<FormElement> params) {
        this.name = name;
        this.jndi = jndi;
        this.params = params;
    }

    public TaskJndiInfo(String name, String jndi) {
        this.name = name;
        this.jndi = jndi;
    }

    public TaskJndiInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    @Override
    public String toString() {
        return "TaskJndiInfo{" +
                "name='" + name + '\'' +
                ", jndi='" + jndi + '\'' +
                '}';
    }

    public List<FormElement> getParams() {
        return params;
    }

    public void setParams(List<FormElement> params) {
        this.params = params;
    }
}
