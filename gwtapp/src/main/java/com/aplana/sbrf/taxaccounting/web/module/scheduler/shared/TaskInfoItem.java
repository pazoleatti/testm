package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import java.io.Serializable;

/**
 * Информация о задаче, доступной для планирования
 * @author dloshkarev
 */
public class TaskInfoItem implements Serializable {
    private static final long serialVersionUID = 348462594878912721L;

    /** Название задачи */
    private String name;

    /** JNDI-имя задачи */
    private String jndi;

    public TaskInfoItem(String name, String jndi) {
        this.name = name;
        this.jndi = jndi;
    }

    public TaskInfoItem() {
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
        return "TaskInfoItem{" +
                "name='" + name + '\'' +
                ", jndi='" + jndi + '\'' +
                '}';
    }
}
