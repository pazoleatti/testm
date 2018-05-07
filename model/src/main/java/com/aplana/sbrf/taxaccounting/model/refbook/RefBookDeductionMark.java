package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Признак кода вычета"
 *
 * @author dloshkarev
 */
public class RefBookDeductionMark extends RefBookSimple<Long> {
    //Код
    private String code;
    //Наименование дохода
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
