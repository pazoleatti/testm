package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Справочник "Статусы налогоплательщика"
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefBookTaxpayerState extends RefBookSimple<Long> {
    //Код
    private String code;
    //Наименование
    private String name;

    public RefBookTaxpayerState(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
