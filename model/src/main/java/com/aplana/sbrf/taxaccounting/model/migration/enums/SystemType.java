package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Тип системы
 * <p/>
 * Старые коды из БД
 * <p/>
 * Новые коды  из
 * "3.8	Приложение №8.
 * Список Новые коды АС
 * (Справочник «Справочник автоматизированных систем» ЦАС НСИ)"
 */
public enum SystemType implements Serializable {
    GAMMA(24, 109, "00", "9", 126),
    GAMMA_01(43, 109, "01", "9", 701),
    GAMMA_02(44, 109, "02", "9", 702),
    GAMMA_03(45, 109, "03", "9", 703),
    GAMMA_04(46, 109, "04", "9", 704),
    GAMMA_05(47, 109, "05", "9", 705),
    GAMMA_06(48, 109, "06", "9", 706),
    GAMMA_07(49, 109, "07", "9", 707),
    GAMMA_08(50, 109, "08", "9", 708),
    GAMMA_09(51, 109, "09", "9", 709),
    GAMMA_10(52, 109, "10", "9", 710),
    GAMMA_11(58, 109, "11", "9", 711),
    GAMMA_12(59, 109, "12", "9", 712),
    DC(36, 701, "00", "P", 123),
    DC_01(37, 701, "01", "Q", 666),
    DC_02(38, 701, "02", "R", 667),
    DC_03(39, 701, "03", "S", 668);

    private static final long serialVersionUID = 1L;

    private final int codeOld;
    private final int codeNew;
    private final String subCodeId;
    private final String sysCodeCharOld;
    private final int depCode;  //код департамента    //TODO (aivanov) поменять на настоящие значения

    private SystemType(int codeOld, int codeNew, String subCodeId, String sysCodeCharOld, int depCode) {
        this.codeOld = codeOld;
        this.codeNew = codeNew;
        this.subCodeId = subCodeId;
        this.depCode = depCode;
        this.sysCodeCharOld = sysCodeCharOld;
    }

    public static int getNewCodeByOldCode(int code) {
        for (SystemType t : values()) {
            if (t.codeOld == code) {
                return t.codeNew;
            }
        }
        throw new IllegalArgumentException("Bad Old System Type " + code);
    }

    public static int getDepCode(int codeOld, String subCodeId) {
        for (SystemType t : values()) {
            if (t.codeOld == codeOld && t.getSubCodeId().equals(subCodeId)) {
                return t.depCode;
            }
        }
        throw new IllegalArgumentException("Номер подсистемы не найден: " + codeOld + " с подкодом" + subCodeId);
    }

    public int getCodeNew() {
        return codeNew;
    }

    public int getCodeOld() {
        return codeOld;
    }

    public String getSubCodeId() {
        return subCodeId;
    }

    public int getDepCode() {
        return depCode;
    }

    public String getSysCodeCharOld() {
        return sysCodeCharOld;
    }
}
