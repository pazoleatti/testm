package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Соотвествие года к значению в новой системе
 */
public enum YearCode implements Serializable {
    YEAR_08(2008, "08", "W", 10281),
    YEAR_09(2009, "09", "X", 10282),
    YEAR_10(2010, "10", "Y", 10283),
    YEAR_11(2011, "11", "Z", 10284),
    YEAR_12(2012, "12", "+", 10080),
    YEAR_13(2013, "13", "-", 13);

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
        throw new IllegalArgumentException("Is not contained in enum YearCode! year:" + year);
    }

    public static YearCode fromYearCut(String yearCut) {
        for (YearCode t : values()) {
            if (t.yearCut.equals(yearCut)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum YearCode! yearCut: " + yearCut);
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
