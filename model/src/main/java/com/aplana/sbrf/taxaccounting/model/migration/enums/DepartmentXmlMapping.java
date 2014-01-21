package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие подразделений
 * Используется при маппинге xml файлов
 *
 */
public enum DepartmentXmlMapping implements Serializable {
    DEP_118_1("996100010", 109, "00", 118),
    DEP_118_2("996100010", 701, "00", 118),
    DEP_118_3("996100010", 1500, "00", 118),

    DEP_701_00("999300010", 701, "00", 123),
    DEP_701_01("999300010", 701, "01", 143),
    DEP_701_02("999300010", 701, "02", 144),
    DEP_701_03("999300010", 701, "03", 145),

    DEP_109_00("999300020", 109, "00", 126),
    DEP_109_01("999300020", 109, "01", 146),
    DEP_109_02("999300020", 109, "02", 147),
    DEP_109_03("999300020", 109, "03", 148),
    DEP_109_04("999300020", 109, "04", 149),
    DEP_109_05("999300020", 109, "05", 150),
    DEP_109_06("999300020", 109, "06", 151),
    DEP_109_07("999300020", 109, "07", 152),
    DEP_109_08("999300020", 109, "08", 153),
    DEP_109_09("999300020", 109, "09", 154),
    DEP_109_10("999300020", 109, "10", 155),
    DEP_109_11("999300020", 109, "11", 156),
    DEP_109_12("999300020", 109, "12", 157),
    DEP_109_13("999300020", 109, "13", 158),
    DEP_109_14("999300020", 109, "14", 159);

    private static final long serialVersionUID = 1L;

    private final String depCode;       // значение подразделения в старой системе
    private final int sysCodeNew;       // код системы
    private final String subSysCode;    // код подсистемы
    private final int departmentId;     // значение подразделения в новой системе

    private DepartmentXmlMapping(String depCodeOld, int sysCodeNew, String subSysCode, int departmentId) {
        this.depCode = depCodeOld;
        this.sysCodeNew = sysCodeNew;
        this.subSysCode = subSysCode;
        this.departmentId = departmentId;
    }

    public static int getNewDepCode(String depCode, int sysCodeNew, String subSysCode) {
        if (DEP_118_1.getDepCode().equals(depCode)) {
            return DEP_118_1.departmentId;
        }
        for (DepartmentXmlMapping t : values()) {

            if (t.depCode.equals(depCode) && t.sysCodeNew == sysCodeNew && t.subSysCode.equals(subSysCode)) {
                return t.departmentId;
            }
        }
        throw new IllegalArgumentException("Incorrect parametres: depCode -" + depCode+ " sysCodeNew - " + sysCodeNew + " subSysCode - " +subSysCode);
    }

    public String getDepCode() {
        return depCode;
    }

    public int getSysCodeNew() {
        return sysCodeNew;
    }

    public String getSubSysCode() {
        return subSysCode;
    }

    public int getDepartmentId() {
        return departmentId;
    }
}
