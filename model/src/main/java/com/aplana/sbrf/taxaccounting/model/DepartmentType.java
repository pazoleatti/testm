package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Типы подразделений банка
 */
public enum DepartmentType implements Serializable {
    /**
     * 0 Искусственное значение, чтобы нумерация шла с нуля для удобного маппинга Enum на таблицу в базе данных.
     */
    @JsonProperty("unknown")
    UNKNOWN("", 0),
    /**
     * 1 Банк - ROOT_BANK
     */
    @JsonProperty("rootBank")
    ROOT_BANK("Банк", 1),
    /**
     * 2 Территориальный банк - TERR_BANK
     */
    @JsonProperty("terrBank")
    TERR_BANK("Территориальный банк", 2),
    /**
     * 3 ЦСКО, ПЦП - CSKO_PCP
     */
    @JsonProperty("cskoPcp")
    CSKO_PCP("ЦСКО, ПЦП", 3),
    /**
     * 4 Управление - MANAGEMENT
     */
    @JsonProperty("management")
    MANAGEMENT("Управление", 4),
    /**
     * 5 INTERNAL - не передается в СУДИР
     */
    @JsonProperty("internal")
    INTERNAL("", 5);

    private static final long serialVersionUID = 1L;

    private final String label;
    private final int code;

    DepartmentType(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public static DepartmentType fromCode(int code) {
        if (RefBook.FAKE_ID == code) {
            return null;
        }
        for (DepartmentType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown department type: " + code);
    }

    /**
     * Описание типа подразделения
     */
    public String getLabel() {
        return label;
    }

    /**
     * Числовое представление типа, используется для хранения в БД
     */
    public int getCode() {
        return code;
    }
}
