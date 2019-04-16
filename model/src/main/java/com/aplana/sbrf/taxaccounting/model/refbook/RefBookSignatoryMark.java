package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Справочник "Признак лица, подписавшего документ"
 */
@Getter
@Setter
public class RefBookSignatoryMark extends RefBookSimple<Long> {
    private Integer code;
    private String name;

    public RefBookSignatoryMark id(Long id) {
        this.id = id;
        return this;
    }
}
