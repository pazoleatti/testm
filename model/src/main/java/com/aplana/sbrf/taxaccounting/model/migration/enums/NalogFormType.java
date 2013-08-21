package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Вид налоговой формы
 */
public enum NalogFormType implements Serializable {
    RNU25(25, 324, "852-25"),
    RNU26(26, 325, "852-26"),
    RNU27(27, 326, "852-27"),
    RNU31(31, 328, "852-31"),
    RNU51(51, 345, "852-51"),
    RNU53(53, 346, "852-53"),
    RNU54(54, 347, "852-54"),
    RNU59(59, 350, "852-59"),
    RNU60(60, 351, "852-60"),
    RNU64(64, 355, "852-64");

    private static final long serialVersionUID = 1L;
    private static final String ERROR = "Неизвестная налоговая форма: ";

    private final int codeOld;
    private final int codeNew;
    private final String codeNewXml;

    private NalogFormType(int codeOld, int codeNew, String codeNewXml) {
        this.codeOld = codeOld;
        this.codeNew = codeNew;
        this.codeNewXml = codeNewXml;
    }

    public static int getNewCodefromOldCode(int code) {
        for (NalogFormType t : values()) {
            if (t.codeOld == code) {
                return t.codeNew;
            }
        }
        throw new IllegalArgumentException(ERROR + code);
    }

    public static String getNewXmlCode(int code) {
        for (NalogFormType t : values()) {
            if (t.codeOld == code) {
                return t.codeNewXml;
            }
        }
        throw new IllegalArgumentException(ERROR + code);
    }

    public static NalogFormType getByCode(int code) {
        for (NalogFormType t : values()) {
            if (t.codeOld == code) {
                return t;
            }
        }
        throw new IllegalArgumentException(ERROR + code);
    }

    public int getCodeNew() {
        return codeNew;
    }

    public int getCodeOld() {
        return codeOld;
    }

    public String getCodeNewXml() {
        return codeNewXml;
    }
}
