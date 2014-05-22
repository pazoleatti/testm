package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

/**
 * Типы назначений
 *
 * @author dloskarev
 */
public enum AppointmentType {
    SOURCES("источников"),
    RECIPIENT("приемников");

    private String name;

    private AppointmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
