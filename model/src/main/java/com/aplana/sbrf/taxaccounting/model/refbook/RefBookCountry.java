package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * @author Andrey Drunk
 */
public class RefBookCountry extends RefBookSimple<Long> {
    //Код
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
