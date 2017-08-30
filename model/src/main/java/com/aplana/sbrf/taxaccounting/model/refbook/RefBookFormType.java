package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Виды налоговых форм
 * @author dloshkarev
 */
public class RefBookFormType extends RefBookSimple<Long> {
    //Наименование
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
