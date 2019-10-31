package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Справочник "Признак лица, подписавшего документ"
 */
@Getter
@Setter
public class RefBookSignatoryMark extends RefBookSimple<Long> {
    public final static Integer TAX_AGENT = 1;
    public final static Integer TAX_AGENT_AMBASSADOR = 2;

    private Integer code;
    private String name;

    public RefBookSignatoryMark id(Long id) {
        this.id = id;
        return this;
    }
}
