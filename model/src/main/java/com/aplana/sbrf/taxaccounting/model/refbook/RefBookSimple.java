package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.EqualsAndHashCode;

/**
 * Простой базовый класс для справочников
 */
@EqualsAndHashCode(callSuper = true)
public abstract class RefBookSimple<IdType extends Number> extends IdentityObject<IdType> {
}
