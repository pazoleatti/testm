package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы пользовательских параметров для задач планировщика
 * @author dloshkarev
 */
public enum TaskParamTypeValues {
    INT(0, "Integer"),
    LONG(1, "Long"),
    FLOAT(2, "Float"),
    DOUBLE(3, "Double"),
    BOOLEAN(4, "Boolean"),
    STRING(5, "String"),
    DATE(6, "Date");

    private final int id;
    private final String name;

    private TaskParamTypeValues(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TaskParamTypeValues fromId(int typeId) {
        for (TaskParamTypeValues type: values()) {
            if (type.id == typeId) {
                return type;
            }
        }
        throw new IllegalArgumentException("Wrong TaskParamTypeValues id: " + typeId);
    }
}
