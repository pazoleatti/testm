package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Тип КНФ
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class RefBookKnfType extends RefBookSimple<Integer> {
    // Наименование типа КНФ
    String name;

    public RefBookKnfType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final RefBookKnfType ALL = new RefBookKnfType(1, "КНФ по всем данным");
    public static final RefBookKnfType BY_NONHOLDING_TAX = new RefBookKnfType(2, "КНФ по неудержанному налогу");
    public static final RefBookKnfType BY_KPP = new RefBookKnfType(3, "КНФ по обособленному подразделению");
    public static final RefBookKnfType BY_PERSON = new RefBookKnfType(4, "КНФ по ФЛ");
    public static final RefBookKnfType FOR_APP2 = new RefBookKnfType(5, "КНФ для Приложения 2");
}
