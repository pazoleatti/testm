package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Справочник "ОК 025-2001 (Общероссийский классификатор стран мира)"
 * @author Andrey Drunk
 */
@Getter @Setter @NoArgsConstructor
public class RefBookCountry extends RefBookSimple<Long> {
    private String code;
    private String name;

    public RefBookCountry(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
