package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * АСНУ
 * @author dloshkarev
 */
public class RefBookAsnu extends RefBookSimple<Integer> {
    private String name;
    private String code;
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
