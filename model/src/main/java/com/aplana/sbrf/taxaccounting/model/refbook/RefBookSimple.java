package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Простой базовый класс для справочников
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class RefBookSimple<IdType extends Number> extends IdentityObject<IdType> {
    public RefBookSimple(IdType id) {
        super(id);
    }
}
