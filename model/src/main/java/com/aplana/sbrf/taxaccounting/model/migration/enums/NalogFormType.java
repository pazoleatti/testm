package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Вид налоговой формы
 */
public enum NalogFormType implements Serializable {
    RNU25(25, 324, "852-25", "250"),
    RNU26(26, 325, "852-26", "260"),
    RNU27(27, 326, "852-27", "270"),
    RNU31(31, 328, "852-31", "310"),
    RNU51(51, 345, "852-51", "510"),
    RNU53(53, 346, "852-53", "530"),
    RNU54(54, 347, "852-54", "540"),
    RNU59(59, 350, "852-59", "590"),
    RNU60(60, 351, "852-60", "600"),
    RNU64(64, 355, "852-64", "640");

    private static final long serialVersionUID = 1L;
    private static final String ERROR = "NalogFormType id is not correct: ";
    private static final String ERROR_NNN = "NalogFormType nnn is not correct: ";

    private final int id;
    private final int codeNew;
    private final String codeNewXml;
    private final String stringNNN;

    private NalogFormType(int id, int codeNew, String codeNewXml, String stringNNN) {
        this.id = id;
        this.codeNew = codeNew;
        this.codeNewXml = codeNewXml;
        this.stringNNN = stringNNN;
    }

    public static int getNewCodeByNNN(String nnn) {
        for (NalogFormType t : values()) {
            if (t.stringNNN.equals(nnn)) {
                return t.codeNew;
            }
        }
        throw new IllegalArgumentException(ERROR_NNN + nnn);
    }

    public static int getIdByNNN(String nnn) {
        for (NalogFormType t : values()) {
            if (t.stringNNN.equals(nnn)) {
                return t.id;
            }
        }
        throw new IllegalArgumentException(ERROR_NNN + nnn);
    }

    public static String getNewXmlCode(int id) {
        for (NalogFormType t : values()) {
            if (t.id == id) {
                return t.codeNewXml;
            }
        }
        throw new IllegalArgumentException(ERROR + id);
    }

    public static NalogFormType getById(int id) {
        for (NalogFormType t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        throw new IllegalArgumentException(ERROR + id);
    }

    public int getCodeNew() {
        return codeNew;
    }

    public int getId() {
        return id;
    }

    public String getCodeNewXml() {
        return codeNewXml;
    }

    public String getStringNNN() {
        return stringNNN;
    }
}
