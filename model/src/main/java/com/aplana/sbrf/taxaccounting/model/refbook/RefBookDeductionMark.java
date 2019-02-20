package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Справочник "Признак кода вычета"
 */
@Getter
@Setter
public class RefBookDeductionMark extends RefBookSimple<Long> {
    public final static Integer STANDARD_CODE = 1;
    public final static Integer SOCIAL_CODE = 2;
    public final static Integer INVESTMENT_CODE = 3;
    public final static Integer PROPERTY_CODE = 4;
    public final static Integer OTHERS_CODE = 5;

    //Код
    private Integer code;
    //Наименование дохода
    private String name;
}
