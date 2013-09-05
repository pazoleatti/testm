package com.aplana.sbrf.taxaccounting.model.migration.enums;

import java.io.Serializable;

/**
 * Маппинг периода отчета для файлов rnu
 * применяется при маппинге ТФ в новую систему
 */
public enum PeriodMapping implements Serializable {
    K_1("1", "m01", 21, 1),    //январь
    K_2("2", "m02", 21, 2),
    K_3("3", "m03", 21, 3),
    K_4("4", "m04", 31, 4),
    K_5("5", "m05", 31, 5),
    K_6("6", "m06", 31, 6),
    K_7("7", "m07", 33, 7),
    K_8("8", "m08", 33, 8),
    K_9("9", "m09", 33, 9),
    K_10("A", "m10", 34, 10),
    K_11("B", "m11", 34, 11),
    K_12("C", "m12", 34, 12),  //декабрь
    K_13("D", "q03", 21, -1),  //1 квартал
    K_14("E", "q06", 31, -1),  //2 квартал
    K_15("F", "q09", 33, -1),  //3 квартал
    K_16("G", "q12", 34, -1),  //4 квартал
    K_17("W", "y08", 34, -1),  //2008 год
    K_18("X", "y09", 34, -1),  //2009 год
    K_19("Y", "y10", 34, -1),  //2010 год
    K_20("Z", "y11", 34, -1),  //2011 год
    K_21("+", "y12", 34, -1);  //2012 год

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String codeXml;
    private final int dictTaxPeriodId;
    private final int dictTaxPeriodIdForMonthly;

    private PeriodMapping(String code, String codeXml, int dictTaxPeriodId, int dictTaxPeriodIdForMonthly) {
        this.code = code;
        this.codeXml = codeXml;
        this.dictTaxPeriodId = dictTaxPeriodId;
        this.dictTaxPeriodIdForMonthly = dictTaxPeriodIdForMonthly;
    }

    public static PeriodMapping fromCode(String code) {
        for (PeriodMapping t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum PeriodMapping! code: " + code);
    }

    public static PeriodMapping fromCodeXml(String codeXml) {
        for (PeriodMapping t : values()) {
            if (t.codeXml.equals(codeXml)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Is not contained in enum PeriodMapping! codeXml " + codeXml);
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

    public String getCodeXml() {
        return codeXml;
    }
}
