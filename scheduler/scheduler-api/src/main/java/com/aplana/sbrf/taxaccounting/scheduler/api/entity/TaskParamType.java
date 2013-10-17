package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import java.util.Date;

/**
 * Типы пользовательских параметров для задач планировщика
 * @author dloshkarev
 */
public enum TaskParamType {

    UNKNOWN(-1, Object.class),
    INT(0, Integer.class),
    LONG(1, Long.class),
    FLOAT(2, Float.class),
    DOUBLE(3, Double.class),
    BOOLEAN(4, Boolean.class),
    STRING(5, String.class),
    DATE(6, Date.class);

    private Integer id;
    private Class<?> typeClass;

    private TaskParamType(Integer id, Class<?> typeClass) {
        this.id = id;
        this.typeClass = typeClass;
    }

    public static TaskParamType getTypeById(Integer id) {
        TaskParamType type = TaskParamType.UNKNOWN;
        for (TaskParamType item : TaskParamType.values()) {
            if (item.getId().equals(id)) {
                type = item;
                break;
            }
        }
        return type;
    }

    public Integer getId() {
        return id;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }
}
