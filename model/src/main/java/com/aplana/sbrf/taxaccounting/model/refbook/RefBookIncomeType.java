package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Справочник "Коды видов доходов"
 */
@Setter
@Getter
@ToString
public class RefBookIncomeType extends RefBookSimple<Long> {
    //Код
    private String code;
    //Наименование дохода
    private String name;
    //Включается в Приложение 2
    private boolean app2Include;
}
