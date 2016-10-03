package com.aplana.sbrf.taxaccounting.model;

/**
 *
 */
public enum DepartmentChangeOperationType {
    CREATE(0),
    UPDATE(1),
    DELETE(2);

    private final int code;

    private DepartmentChangeOperationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DepartmentChangeOperationType fromCode(int code) {
        for (DepartmentChangeOperationType t: values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown department change operation type: " + code);
    }

}
