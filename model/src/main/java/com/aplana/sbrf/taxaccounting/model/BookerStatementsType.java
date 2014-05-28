package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип формы для журнала аудита
 *
 * @author lhaziev
 */
public enum BookerStatementsType {

    INCOME101(0, "Форма 101"),
    INCOME102(1, "Форма 102");

    private int id;
    private String name;

    BookerStatementsType(int id, String name) {
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
}
