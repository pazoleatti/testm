package com.aplana.sbrf.taxaccounting.model;

/**
 * Список полей для журнала аудита
 *
 * @author LHaziev
 */
public enum AuditFieldList {

    ALL(1, "Все поля"),
    FORM_TYPE(2, "Вид налоговой формы"),
    DECLARATION_TYPE(3, "Вид декларации"),
    TYPE(12, "Тип формы"),
    PERIOD(4, "Период"),
    DEPARTMENT(5, "Подразделение"),
    USER(6, "Пользователь"),
    ROLE(7, "Роль пользователя"),
    EVENT(8, "Событие"),
    NOTE(9, "Текст события"),
    FORM_KIND(10, "Тип налоговой формы"),
    IP(11, "IP пользователя");


    private long id;
    private String name;

    AuditFieldList(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static AuditFieldList fromId(long fieldId) {
        for (AuditFieldList kind: values()) {
            if (kind.id == fieldId) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AuditFieldList id: " + fieldId);
    }
}
