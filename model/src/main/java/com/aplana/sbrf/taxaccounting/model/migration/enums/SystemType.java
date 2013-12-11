package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Тип системы
 */
public enum SystemType implements Serializable {
    GAMMA(13, 109, "00", "9"),          //Учет операций с ЦБ
    GAMMA_01(154753, 109, "01", "9"),   // ее подсистемы
    GAMMA_02(154754, 109, "02", "9"),
    GAMMA_03(154755, 109, "03", "9"),
    GAMMA_04(154756, 109, "04", "9"),
    GAMMA_05(154757, 109, "05", "9"),
    GAMMA_06(154758, 109, "06", "9"),
    GAMMA_07(154759, 109, "07", "9"),
    GAMMA_08(154760, 109, "08", "9"),
    GAMMA_09(154761, 109, "09", "9"),
    GAMMA_10(200720, 109, "10", "9"),
    GAMMA_11(229395, 109, "11", "9"),
    GAMMA_12(229396, 109, "12", "9"),
    GAMMA_13(245351, 109, "13", "9"),
    GAMMA_14(301831, 109, "14", "9"),
    DC(55147, 701, "00", "P"),          // Diasoft
    DC_01(43407, 701, "01", "Q"),       // ее подсистемы
    DC_02(43408, 701, "02", "R"),
    DC_03(43409, 701, "03", "S"),
    TN(14, 1500, "00", "A");            //Ведение РНУ

    private static final long serialVersionUID = 1L;

    private final int id;               // id в таблице
    private final int codeNew;          // новый код системы
    private final String subCode;       // код подсистемы
    private final String sysCodeChar;   // сисмвол системы для указания в названии ТФ

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
