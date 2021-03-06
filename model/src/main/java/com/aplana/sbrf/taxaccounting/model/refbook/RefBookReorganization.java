package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Коды форм реорганизации (ликвидации) организации"
 */
public class RefBookReorganization extends RefBookSimple<Long> {
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
