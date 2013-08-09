package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

/**
 * Типы справочников
 *
 * @author Stanislav Yasinskiy
 */
public enum Type {
    EXTERNAL("Внешний справочник"),
    INTERNAL("Внутренний справочник");

    private final String name;

    private Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
