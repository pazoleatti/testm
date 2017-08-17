package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Типы налоговых форм
 * @author dloshkarev
 */
public class RefBookDeclarationKind extends RefBookSimple<Long> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
