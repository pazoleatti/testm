package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Коды видов доходов
 * @author dloshkarev
 */
public class RefBookIncomeType extends RefBookSimple<Long> {
    public static final int REF_BOOK_ID = 922;

    private String code;
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
