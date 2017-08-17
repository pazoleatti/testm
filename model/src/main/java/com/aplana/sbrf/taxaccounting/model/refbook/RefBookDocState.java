package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Состояние ЭД
 * @author dloshkarev
 */
public class RefBookDocState extends RefBookSimple<Long> {
    private String name;
    private String knd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKnd() {
        return knd;
    }

    public void setKnd(String knd) {
        this.knd = knd;
    }
}
