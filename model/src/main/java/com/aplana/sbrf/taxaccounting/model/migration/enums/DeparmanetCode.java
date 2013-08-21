package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие подразделений
 *
 */
public enum DeparmanetCode implements Serializable {
    DEP_118_1("996100010", 109, "00", 118),
    DEP_118_2("996100010", 701, "00", 118),

    DEP_701_00("999300010", 701, "00", 123),
    DEP_701_01("999300010", 701, "01", 801),       //TODO depCodeNew с 801 по 803 заменить на настоящее значение
    DEP_701_02("999300010", 701, "02", 802),
    DEP_701_03("999300010", 701, "03", 803),

    DEP_109_00("999300020", 109, "00", 126),
    DEP_109_01("999300020", 109, "01", 901),      //TODO depCodeNew с 901 по 912 заменить на настоящее значение
    DEP_109_02("999300020", 109, "02", 902),
    DEP_109_03("999300020", 109, "03", 903),
    DEP_109_04("999300020", 109, "04", 904),
    DEP_109_05("999300020", 109, "05", 905),
    DEP_109_06("999300020", 109, "06", 906),
    DEP_109_07("999300020", 109, "07", 907),
    DEP_109_08("999300020", 109, "08", 908),
    DEP_109_09("999300020", 109, "09", 909),
    DEP_109_10("999300020", 109, "10", 910),
    DEP_109_11("999300020", 109, "11", 911),
    DEP_109_12("999300020", 109, "12", 912);

    private static final long serialVersionUID = 1L;

    private final String depCodeOld;
    private final int sysCodeNew;
    private final String subSysCode;
    private final int depCodeNew;

    private DeparmanetCode(String depCodeOld, int sysCodeNew, String subSysCode, int depCodeNew) {
        this.depCodeOld = depCodeOld;
        this.sysCodeNew = sysCodeNew;
        this.subSysCode = subSysCode;
        this.depCodeNew = depCodeNew;
    }

    public static int getNewDepCode(String depCodeOld, int sysCodeNew, String subSysCode) {
        if (DEP_118_1.getDepCodeOld().equals(depCodeOld)) {
            return DEP_118_1.depCodeNew;
        }
        for (DeparmanetCode t : values()) {

            if (t.depCodeOld.equals(depCodeOld) && t.sysCodeNew == sysCodeNew && t.subSysCode.equals(subSysCode)) {
                return t.depCodeNew;
            }
        }
        throw new IllegalArgumentException("Incorrect parametres: depCodeOld -" + depCodeOld + " sysCodeNew - " + sysCodeNew + " subSysCode - " +subSysCode);
    }

    public String getDepCodeOld() {
        return depCodeOld;
    }

    public int getSysCodeNew() {
        return sysCodeNew;
    }

    public String getSubSysCode() {
        return subSysCode;
    }

    public int getDepCodeNew() {
        return depCodeNew;
    }
}
