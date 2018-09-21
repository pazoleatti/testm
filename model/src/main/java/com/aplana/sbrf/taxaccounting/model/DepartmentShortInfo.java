package com.aplana.sbrf.taxaccounting.model;

/**
 * Краткое представление информации о подразделении.
 */
public class DepartmentShortInfo {

    private Integer id;
    private String name;
    private Boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
