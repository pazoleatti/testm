package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие года к значению в новой системе
 */
public enum YearCode implements Serializable {
    YEAR_07(2007, "V"),
    YEAR_08(2008, "W"),
    YEAR_09(2009, "X"),
    YEAR_10(2010, "Y"),
    YEAR_11(2011, "Z"),
    YEAR_12(2012, "+");     //TODO установить правильное значение

    private static final long serialVersionUID = 1L;

    private final int year;
    private final String code;

    private YearCode(int year, String code) {
        this.year = year;
        this.code = code;
    }

    public static String fromYear(int year) {
        for (YearCode t : values()) {
            if (t.year == year) {
                return t.code;
            }
        }
        throw new IllegalArgumentException("Не правильное значение года: " + year);
    }

    public int getYear() {
        return year;
    }

    public String getCode() {
        return code;
    }
}
