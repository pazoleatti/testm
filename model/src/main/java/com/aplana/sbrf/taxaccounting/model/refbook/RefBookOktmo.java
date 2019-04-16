package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Запись справочника ОКТМО
 */
@Getter
@Setter
public class RefBookOktmo extends RefBookSimple<Long> {
    // Код
    private String code;
    // Наименование
    private String name;
    // Раздел
    private Integer section;

    public RefBookOktmo id(Long id) {
        this.id = id;
        return this;
    }

    public RefBookOktmo code(String code) {
        this.code = code;
        return this;
    }
}
