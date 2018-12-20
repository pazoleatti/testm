package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * АСНУ
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefBookAsnu extends RefBookSimple<Long> {
    //Наименование АСНУ
    private String name;
    //Код АСНУ
    private String code;
    //Тип дохода
    private String type;
    //Приоритет
    private Integer priority;

}
