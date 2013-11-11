package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import java.io.Serializable;

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
}
