package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип формы для журнала аудита
 *
 * @author Dmitriy Levykin
 */
public enum AuditFormType {

    FORM_TYPE_TAX(1, "Налоговые формы"),
    FORM_TYPE_DECLARATION(2, "Декларации");

    private int id;
    private String name;

    AuditFormType (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
