package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Моделька для представления "назначений НФ и деклараций"
 *
 * @author sgoryachkin
 */
public class FormTypeKind implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long formTypeId;
    private FormDataKind kind;
    private String name;
    private Department department;
    private Department performer;

    public Department getPerformer() {
        return performer;
    }

    public void setPerformer(Department performer) {
        this.performer = performer;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    /**
     * Получить идентификатор записи
     *
     * @return идентификатор записи
     */
    public Long getId() {
        return id;
    }

    /**
     * Задать идентификатор записи
     *
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }


    /**
     * Получить тип налоговой формы
     *
     * @return тип налоговой формы
     */
    public FormDataKind getKind() {
        return kind;
    }

    /**
     * Задать тип налоговой формы
     *
     * @param kind тип налоговой формы
     */
    public void setKind(FormDataKind kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Long formTypeId) {
        this.formTypeId = formTypeId;
    }
}
