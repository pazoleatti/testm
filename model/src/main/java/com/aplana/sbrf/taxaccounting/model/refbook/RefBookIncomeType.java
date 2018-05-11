package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Коды видов доходов"
 *
 * @author dloshkarev
 */
public class RefBookIncomeType extends RefBookSimple<Long> {
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
