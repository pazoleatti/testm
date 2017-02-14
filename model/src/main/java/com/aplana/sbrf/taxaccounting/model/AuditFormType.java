package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип формы для журнала аудита
 *
 * @author Dmitriy Levykin
 */
public enum AuditFormType {

    FORM_TYPE_TAX(1, "-"),
    FORM_TYPE_DECLARATION(2, "Налоговая форма"),
    FORM_TEMPLATE_VERSION(3, "--"),
    DECLARATION_VERSION(4, "Версия макета налоговой формы"),
    INCOME101(5, "Форма 101"),
    INCOME102(6, "Форма 102");

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
