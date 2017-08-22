package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Типы налоговых форм
 *
 * @author dloshkarev
 */
public class RefBookDeclarationKind extends RefBookSimple<Long> {
    //Наименование
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
