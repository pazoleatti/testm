package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * @author Andrey Drunk
 */
public class RefBookCountry extends RefBookSimple<Long> {

    private String code;

    public RefBookCountry() {
    }

    public RefBookCountry(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(id).append(", ").append(code).append("]").toString();
    }


}
