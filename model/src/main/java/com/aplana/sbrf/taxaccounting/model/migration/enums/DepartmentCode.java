package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие кода подразделения в бд и кода в названии файла
 */
public enum DepartmentCode implements Serializable {
    DEP_1("0013", "130"),
    DEP_2("0022", "220"),
    DEP_3("996100000", "220"),
    DEP_4("996100010", "220"),
    DEP_5("996100020", "220"),
    DEP_6("996100030", "220"),
    DEP_7("996100040", "220"),
    DEP_8("996100050", "220"),
    DEP_9("999300000", "130"),
    DEP_10("999300010", "130"),
    DEP_11("999300020", "130"),
    DEP_12("999300030", "130"),
    DEP_13("999300040", "130"),
    DEP_14("999300050", "130");

    private static final long serialVersionUID = 1L;

    private DepartmentCode(String dataBaseCode, String filenamePartCode) {
        this.dataBaseCode = dataBaseCode;
        this.filenamePartCode = filenamePartCode;
    }

    private final String dataBaseCode;        //значение из БД
    private final String filenamePartCode;    //соответсиве в части названия файла


    public static DepartmentCode fromDataBaseCode(String dataBaseCode) {
        for (DepartmentCode t : values()) {
            if (t.dataBaseCode.equals(dataBaseCode)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum DepartmentCode! dataBaseCode: " + dataBaseCode);
    }

    public String getDataBaseCode() {
        return dataBaseCode;
    }

    public String getFilenamePartCode() {
        return filenamePartCode;
    }
}
