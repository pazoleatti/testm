package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Типы подразделений"
 *
 * @author dloshkarev
 */
public class RefBookDepartmentType extends RefBookSimple<Long> {
    //Наименование
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
