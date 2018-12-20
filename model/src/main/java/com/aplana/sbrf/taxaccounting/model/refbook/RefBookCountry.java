package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Справочник "ОК 025-2001 (Общероссийский классификатор стран мира)"
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefBookCountry extends RefBookSimple<Long> {
    private String code;
    private String name;

    public RefBookCountry(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
