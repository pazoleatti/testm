package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;

/**
 * DTO-Класс, содержащий информацию о параметрах задач планировщика
 * @author dloshkarev
 */
public class TaskSearchResultItem implements Serializable {
    private static final long serialVersionUID = -2958384670394988526L;

    /** Идентификатор задачи */
    private Long id;

    private Long contextId;

    /** Название задачи */
    private String name;

    /** Состояние задачи */
    private String state;

    /** Дата создания задачи */
    private String modificationDate;

    /** Дата следующего запуска задачи */
    private String nextFireTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Long getContextId() {
        return contextId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskSearchResultItem that = (TaskSearchResultItem) o;

        if (contextId != null ? !contextId.equals(that.contextId) : that.contextId != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (modificationDate != null ? !modificationDate.equals(that.modificationDate) : that.modificationDate != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (nextFireTime != null ? !nextFireTime.equals(that.nextFireTime) : that.nextFireTime != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (contextId != null ? contextId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (modificationDate != null ? modificationDate.hashCode() : 0);
        result = 31 * result + (nextFireTime != null ? nextFireTime.hashCode() : 0);
        return result;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
}
