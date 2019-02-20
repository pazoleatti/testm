package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Запись справочника "Коды видов вычетов"
 */
@Getter
@Setter
public class RefBookDeductionType extends RefBookSimple<Long> {
    /**
     * Код вычета
     */
    private String code;
    /**
     * Наименование вычета
     */
    private String name;
    /**
     * Признак вычета
     */
    private RefBookDeductionMark mark;
}
