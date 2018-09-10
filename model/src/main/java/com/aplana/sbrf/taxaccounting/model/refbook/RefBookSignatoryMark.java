package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Признак лица, подписавшего документ"
 */
public class RefBookSignatoryMark extends RefBookSimple<Long> {
    private Integer code;
    private String name;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
