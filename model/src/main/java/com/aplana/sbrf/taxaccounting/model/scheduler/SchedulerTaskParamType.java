package com.aplana.sbrf.taxaccounting.model.scheduler;

/**
 * @author lhaziev
 */
public enum SchedulerTaskParamType {
    STRING(1),
    LONG(2),
    DOUBLE(3);

    private Integer id;

    SchedulerTaskParamType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static SchedulerTaskParamType getById(int id) {
        for (SchedulerTaskParamType f: values()) {
            if (f.id == id) {
                return f;
            }
        }
        throw new IllegalArgumentException("Wrong paramType id: " + id);
    }

}
