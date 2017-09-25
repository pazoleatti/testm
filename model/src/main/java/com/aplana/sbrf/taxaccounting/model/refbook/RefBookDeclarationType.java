package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Виды форм
 * Created by aokunev on 10.08.2017.
 */
public class RefBookDeclarationType extends RefBookVersionedObject<Long> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
