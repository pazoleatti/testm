package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип формы для журнала аудита
 *
 * @author Dmitriy Levykin
 */
public enum AuditFormType {

    FORM_TYPE_TAX(1, "Налоговая форма"),
    FORM_TYPE_DECLARATION(2, "Декларация"),
    FORM_TEMPLATE_VERSION(3, "Версия макета НФ"),
    DECLARATION_VERSION(4, "Версия макета декларации"),
    INCOME101(5, BookerStatementsType.INCOME101.getName()),
    INCOME102(6, BookerStatementsType.INCOME102.getName());

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

    public static AuditFormType fromId(int typeId) {
        for (AuditFormType type: values()) {
            if (type.id == typeId) {
                return type;
            }
        }
        throw new IllegalArgumentException("Wrong AuditFormType id: " + typeId);
    }
}
