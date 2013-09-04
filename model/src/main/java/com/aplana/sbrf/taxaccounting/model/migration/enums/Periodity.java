package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Периодичность
 */
public enum Periodity implements Serializable {
    YEAR(1, "Ежегодно"),
    QUARTER(4, "Ежеквартально"),
    MONTH(5, "Ежемесячно"),
    DAY(8, "Ежедневно"),
    WORKDAY(10, "По рабочим дням");

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String desc;

    private Periodity(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static Periodity fromId(int id) {
        for (Periodity t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum Periodity! id: " + id);
    }

}
