package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

/**
 * Объект ответственный за создание алиаса который будет
 * подставлен в sql запрос
 *
 * @author auldanov on 15.08.2014.
 */
public interface AttributeAliasBuilder {
    String buildAlias(String tableAlias, RefBookAttribute attribute);
}
