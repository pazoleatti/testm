package com.aplana.sbrf.taxaccounting.model;

/**
 * @author Fail Mukhametdinov
 */
public enum  AutoNumerationColumnType {
    SERIAL(0, "Последовательная"),
    CROSS(1, "Сквозная");

    private int type;
    private String name;

    AutoNumerationColumnType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
