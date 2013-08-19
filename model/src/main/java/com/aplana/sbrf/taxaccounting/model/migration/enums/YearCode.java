package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие года к значению в новой системе
 */
public enum YearCode implements Serializable {
    YEAR_08(2008, 88),    //TODO (aivanov) поменять code на настоящие значения
    YEAR_09(2009, 89),
    YEAR_10(2010, 90),
    YEAR_11(2011, 91),
    YEAR_12(2012, 92);

    private static final long serialVersionUID = 1L;

    private final int year;
    private final int code;

    private YearCode(int year, int code) {
        this.year = year;
        this.code = code;
    }

    public static int fromYear(int year) {
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

    public int getCode() {
        return code;
    }
}
