package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Виды налоговых форм
 * @author dloshkarev
 */
public class RefBookFormType extends RefBookSimple<Long> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
