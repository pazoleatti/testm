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
        return name;
    }

    public List<FormElement> getParams() {
        return params;
    }

    public void setParams(List<FormElement> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskJndiInfo that = (TaskJndiInfo) o;

        if (jndi != null ? !jndi.equals(that.jndi) : that.jndi != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (params != null ? !params.equals(that.params) : that.params != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (jndi != null ? jndi.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
