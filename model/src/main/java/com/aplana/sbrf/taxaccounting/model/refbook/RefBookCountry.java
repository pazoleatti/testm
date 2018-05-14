package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "ОК 025-2001 (Общероссийский классификатор стран мира)"
 * @author Andrey Drunk
 */
public class RefBookCountry extends RefBookSimple<Long> {
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
