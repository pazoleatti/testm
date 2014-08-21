package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import org.springframework.stereotype.Service;

/**
 * Строитель join части sql выражения,
 * когда универсальный справочник ссылается на другой универсальный справочник
 *
 * @author auldanov on 15.08.2014.
 */
@Service
public class Universal2UniversalJoinSqlPartBuilder implements JoinSqlPartBuilder {
    @Override
    public String build(String foreignTableAlias, JoinPoint joinPoint, boolean firstAttributeInChain) {
        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = joinPoint.getReferenceAttribute().getAlias();

        // алиас аттрибута справочника который является ссылкой с учетом алиаса таблицы
        String linkAttributeAliasWithTableAlias = firstAttributeInChain ?
                "a" + linkAttributeAlias + ".reference_value" :
                foreignTableAlias + "." + linkAttributeAlias;

        // составления join запроса
        return new StringBuilder()
                .append("left join ref_book_value ")
                .append(foreignTableAlias)
                .append(" on ")
                .append(foreignTableAlias)
                .append(".record_id = ")
                .append(linkAttributeAliasWithTableAlias)
                .append(" and ")
                .append(foreignTableAlias)
                .append(".attribute_id = ")
                .append(joinPoint.getDestinationAttribute().getId())
                .append("\n")
                .toString();
    }
}
