package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие года к значению в новой системе
 */
public enum YearCode implements Serializable {
    YEAR_08(2008, "08", "W", 7),      //TODO установить правильное значение за место 7
    YEAR_09(2009, "09", "X", 8),      //TODO установить правильное значение за место 8
    YEAR_10(2010, "10", "Y", 9),      //TODO установить правильное значение за место 9
    YEAR_11(2011, "11", "Z", 10),     //TODO установить правильное значение за место 10
    YEAR_12(2012, "12", "+", 11);     //TODO установить правильное значение за место + и 11

    private static final long serialVersionUID = 1L;

    private final int year;
    private final String yearCut;    //используется в xml
    private final String code;
    private final int taxPeriodId;

    private YearCode(int year, String yearCut, String code, int taxPeriodId) {
        this.year = year;
        this.yearCut = yearCut;
        this.code = code;
        this.taxPeriodId = taxPeriodId;
    }

    public static YearCode fromYear(int year) {
        for (YearCode t : values()) {
            if (t.year == year) {
                return t;
            }
        }
        throw new IllegalArgumentException("Incorrect year: " + year);
    }

    public static YearCode fromYearCut(String yearCut) {
        for (YearCode t : values()) {
            if (t.yearCut.equals(yearCut)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Incorrect yearCut: " + yearCut);
    }

    public int getYear() {
        return year;
    }

    public String getYearCut() {
        return yearCut;
    }

    public String getCode() {
        return code;
    }

    public int getTaxPeriodId() {
        return taxPeriodId;
    }
}
