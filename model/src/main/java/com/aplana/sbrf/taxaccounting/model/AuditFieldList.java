package com.aplana.sbrf.taxaccounting.model;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Список полей для журнала аудита
 *
 * @author LHaziev
 */
public enum AuditFieldList {

    ALL(1, "Все поля", 1),
    FORM_TYPE(2, "Вид налоговой формы", 8),
    DECLARATION_TYPE(3, "Вид декларации", 9),
    TYPE(12, "Тип формы", 6),
    PERIOD(4, "Период", 4),
    DEPARTMENT(5, "Подразделение", 5),
    USER(6, "Пользователь", 10),
    ROLE(7, "Роль пользователя", 11),
    EVENT(8, "Событие", 2),
    NOTE(9, "Текст события", 3),
    FORM_KIND(10, "Тип налоговой формы", 7),
    IP(11, "IP пользователя", 12);

    private long id;
    private String name;
    private Integer ordering;

    AuditFieldList(long id, String name, int ordering) {
        this.id = id;
        this.name = name;
        this.ordering = ordering;
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

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public static AuditFieldList fromId(long fieldId) {
        for (AuditFieldList kind: values()) {
            if (kind.id == fieldId) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AuditFieldList id: " + fieldId);
    }

    public static AuditFieldList[] getSortedValues() {
        AuditFieldList[] values = values();
        Arrays.sort(values, new Comparator<AuditFieldList>() {
            @Override
            public int compare(AuditFieldList o1, AuditFieldList o2) {
				return o1.getOrdering().compareTo(o2.getOrdering());
            }
        });
        return values;
    }
}