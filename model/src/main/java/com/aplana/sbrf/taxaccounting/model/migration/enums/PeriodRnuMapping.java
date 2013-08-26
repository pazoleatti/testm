package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Маппинг периода отчета для файлов rnu
 * применяется при маппинге ТФ в новую систему
 */
public enum PeriodRnuMapping implements Serializable {
    K_1("1", 21, 1),    //январь
    K_2("2", 21, 2),
    K_3("3", 21, 3),
    K_4("4", 31, 4),
    K_5("5", 31, 5),
    K_6("6", 31, 6),
    K_7("7", 33, 7),
    K_8("8", 33, 8),
    K_9("9", 33, 9),
    K_10("A", 34, 10),
    K_11("B", 34, 11),
    K_12("C", 34, 12),  //декабрь
    K_13("D", 21, -1),  //1 квартал
    K_14("E", 31, -1),  //2 квартал
    K_15("F", 33, -1),  //3 квартал
    K_16("G", 34, -1),  //4 квартал
    K_17("W", 34, -1),  //2008 год
    K_18("X", 34, -1),  //2009 год
    K_19("Y", 34, -1),  //2010 год
    K_20("Z", 34, -1),  //2011 год
    K_21("+", 34, -1);  //2012 год

    private static final long serialVersionUID = 1L;

    private final String code;
    private final int dictTaxPeriodId;
    private final int dictTaxPeriodIdForMonthly;

    private PeriodRnuMapping(String code, int dictTaxPeriodId, int dictTaxPeriodIdForMonthly) {
        this.code = code;
        this.dictTaxPeriodId = dictTaxPeriodId;
        this.dictTaxPeriodIdForMonthly = dictTaxPeriodIdForMonthly;
    }

    public static PeriodRnuMapping fromCode(String code) {
        for (PeriodRnuMapping t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Incorrect period code: " + code);
    }

    public String getCode() {
        return code;
    }

    public int getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public int getDictTaxPeriodIdForMonthly() {
        return dictTaxPeriodIdForMonthly;
    }
}
