package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Тип системы
 */
public enum SystemType implements Serializable {
    GAMMA(24, 109, "00", "9"),
    GAMMA_01(43, 109, "01", "9"),
    GAMMA_02(44, 109, "02", "9"),
    GAMMA_03(45, 109, "03", "9"),
    GAMMA_04(46, 109, "04", "9"),
    GAMMA_05(47, 109, "05", "9"),
    GAMMA_06(48, 109, "06", "9"),
    GAMMA_07(49, 109, "07", "9"),
    GAMMA_08(50, 109, "08", "9"),
    GAMMA_09(51, 109, "09", "9"),
    GAMMA_10(52, 109, "10", "9"),
    GAMMA_11(58, 109, "11", "9"),
    GAMMA_12(59, 109, "12", "9"),
    GAMMA_13(60, 109, "13", "9"),
    DC(36, 701, "00", "P"),
    DC_01(37, 701, "01", "Q"),
    DC_02(38, 701, "02", "R"),
    DC_03(39, 701, "03", "S");

    private static final long serialVersionUID = 1L;

    private final int id;
    private final int codeNew;
    private final String subCode;
    private final String sysCodeChar;

    private SystemType(int id, int codeNew, String subCode, String sysCodeChar) {
        this.id = id;
        this.codeNew = codeNew;
        this.subCode = subCode;
        this.sysCodeChar = sysCodeChar;
    }

    public static SystemType fromId(int id) {
        for (SystemType t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum SystemType! id: " + id);
    }

    public int getId() {
        return id;
    }

    public int getCodeNew() {
        return codeNew;
    }

    public String getSubCode() {
        return subCode;
    }

    public String getSysCodeChar() {
        return sysCodeChar;
    }
}
