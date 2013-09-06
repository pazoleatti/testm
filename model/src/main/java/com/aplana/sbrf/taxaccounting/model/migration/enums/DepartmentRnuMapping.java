package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Маппинг кода подразделения для файлов rnu
 */
public enum DepartmentRnuMapping implements Serializable {
    DEP_1("130", "P", "00", 123),

    DEP_2("130", "S", "03", 145),
    DEP_3("130", "R", "02", 144),
    DEP_4("130", "Q", "01", 143),

    DEP_5("220", "", "", 118),

    DEP_6("130", "9", "00", 126),
    DEP_7("130", "9", "01", 146),
    DEP_8("130", "9", "02", 147),
    DEP_9("130", "9", "03", 148),
    DEP_10("130", "9", "04", 149),
    DEP_11("130", "9", "05", 150),
    DEP_12("130", "9", "06", 151),
    DEP_13("130", "9", "07", 152),
    DEP_14("130", "9", "08", 153),
    DEP_15("130", "9", "09", 154),
    DEP_16("130", "9", "10", 155),
    DEP_17("130", "9", "11", 156),
    DEP_18("130", "9", "12", 157),
    DEP_19("130", "9", "13", 158),
    DEP_20("130", "9", "14", 159);


    private static final long serialVersionUID = 1L;

    private final String stringPPP;
    private final String systemSymbol;
    private final String subSystemString;
    private final int department_id;

    private DepartmentRnuMapping(String stringPPP, String systemSymbol, String subSystemString, int department_id) {
        this.stringPPP = stringPPP;
        this.systemSymbol = systemSymbol;
        this.subSystemString = subSystemString;
        this.department_id = department_id;
    }

    public static int getDepartmentId(String stringPPP, String systemSymbol, String subSystemString) {
        for (DepartmentRnuMapping t : values()) {
            if (DEP_5.getStringPPP().equals(stringPPP)) {
                return DEP_5.department_id;
            } else {
                if (subSystemString == null) {
                    if (t.stringPPP.equals(stringPPP) && t.systemSymbol.equals(systemSymbol)) {
                        return t.department_id;
                    }
                } else {
                    if (t.stringPPP.equals(stringPPP) && t.systemSymbol.equals(systemSymbol) && t.subSystemString.equals(subSystemString)) {
                        return t.department_id;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Incorrect parametres: stringPPP -" + stringPPP+ " systemSymbol - " + systemSymbol + " subSystemString - " + subSystemString);
    }

    public String getStringPPP() {
        return stringPPP;
    }

    public String getSystemSymbol() {
        return systemSymbol;
    }

    public String getSubSystemString() {
        return subSystemString;
    }

    public int getDepartment_id() {
        return department_id;
    }
}
