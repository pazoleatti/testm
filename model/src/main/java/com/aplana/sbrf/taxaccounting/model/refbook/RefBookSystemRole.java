package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Системные роли"
 *
 * @author dloshkarev
 */
public class RefBookSystemRole extends RefBookSimple<Long> {
    //Наименование
    private String name;
    //Алиас
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
