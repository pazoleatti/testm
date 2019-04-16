package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Справочник "Коды места представления расчета"
 */
@Getter
@Setter
public class RefBookPresentPlace extends RefBookSimple<Long> {
    private String code;
    private String name;

    public RefBookPresentPlace id(Long id) {
        this.id = id;
        return this;
    }
}
