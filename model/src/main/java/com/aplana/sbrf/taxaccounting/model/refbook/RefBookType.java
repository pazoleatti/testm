package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Типы справочников
 *
 * @author Stanislav Yasinskiy
 */
public enum RefBookType {

    INTERNAL(0, "Внутренний справочник"),
    EXTERNAL(1, "Внешний справочник");

    private final Integer id;
    private final String title;

    private RefBookType(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
