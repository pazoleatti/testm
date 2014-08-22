package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.springframework.stereotype.Service;

/**
 * Реализация строителя join части sql выражения
 * когда простой справочник ссылается на универсальный
 *
 * @author auldanov on 22.08.2014.
 */
@Service
public class Simple2UniversalJoinSqlPartBuilder implements JoinSqlPartBuilder {
    @Override
    public String build(String foreignTableAlias, JoinPoint joinPoint, boolean firstAttributeInChain) {
        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = joinPoint.getReferenceAttribute().getAlias();

        // составления join запроса
        return new StringBuilder()
                .append("left join ref_book_value ")
                .append(foreignTableAlias)
                .append(" on ")
                .append(foreignTableAlias)
                .append(".record_id = ")
                .append(linkAttributeAlias)
                .append(" and ")
                .append(foreignTableAlias)
                .append(".attribute_id = ")
                .append(joinPoint.getDestinationAttribute().getId())
                .append("\n")
                .toString();
    }
}
