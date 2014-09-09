package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.springframework.stereotype.Service;

/**
 * Реализация локиги создания алиаса для простого справочника
 *
 * @author auldanov on 21.08.2014.
 */
@Service
public class SimpleRefBookAttributeAliasBuilder implements AttributeAliasBuilder {
    @Override
    public String buildAlias(String tableAlias, RefBookAttribute attribute) {
        return new StringBuilder()
                .append(tableAlias)
                .append(".")
                .append(attribute.getAlias())
                .toString();
    }
}
