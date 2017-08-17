package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Created by aokunev on 10.08.2017.
 */
public class RefBookDeclarationType extends RefBookVersionedObject<Integer> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
