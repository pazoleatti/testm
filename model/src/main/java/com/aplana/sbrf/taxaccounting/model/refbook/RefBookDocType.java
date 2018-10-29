package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Справочник "Коды документов"
 *
 * @author dloshkarev
 */
@Getter @Setter @NoArgsConstructor @EqualsAndHashCode(of ={"code", "name"}, callSuper = false)
public class RefBookDocType extends RefBookSimple<Long> {
    //Наименование
    private String name;
    //Код
    private String code;
    //Приоритет
    private Integer priority;

    public RefBookDocType(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
