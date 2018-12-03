package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тип формы для журнала аудита
 *
 * @author Dmitriy Levykin
 */
@AllArgsConstructor
@Getter
public enum AuditFormType {

    FORM_TYPE_TAX(1, "Налоговая форма"),
    FORM_TYPE_DECLARATION(2, "Налоговая форма"),
    FORM_TEMPLATE_VERSION(3, "Версия макета налоговой формы");

    private int id;
    private String name;

    public static AuditFormType fromId(int typeId) {
        for (AuditFormType type : values()) {
            if (type.id == typeId) {
                return type;
            }
        }
        throw new IllegalArgumentException("Wrong AuditFormType id: " + typeId);
    }
}