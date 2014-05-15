package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

/**
 * Типы назначений
 * @author dloskarev
 */
public enum AppointmentType {
    SOURCES("Источников"),
    RECIPIENT("Приемников");

    private String name;

    private AppointmentType( String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
