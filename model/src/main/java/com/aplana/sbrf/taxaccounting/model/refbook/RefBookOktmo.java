package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Запись справочника ОКТМО
 */
@Getter
@Setter
@NoArgsConstructor
public class RefBookOktmo extends RefBookSimple<Long> {
    // Код
    private String code;
    // Наименование
    private String name;
    // Раздел
    private Integer section;

    public RefBookOktmo(Long id) {
        this.id = id;
    }
}
