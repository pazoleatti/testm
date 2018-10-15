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
}
