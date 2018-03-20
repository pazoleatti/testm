package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Виды налоговых форм
 *
 * @author dloshkarev
 */
public class RefBookFormType extends RefBookSimple<Long> {

    private String name;

    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
