package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Коды видов доходов
 *
 * @author dloshkarev
 */
public class RefBookIncomeType extends RefBookSimple<Long> {
    public static final int REF_BOOK_ID = 922;

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
