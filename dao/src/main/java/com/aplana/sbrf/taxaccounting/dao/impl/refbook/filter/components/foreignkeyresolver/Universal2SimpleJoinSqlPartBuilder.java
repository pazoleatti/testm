package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Построитель join части sql выражения,
 * для точки соединения универального справочника с простым справочником
 *
 * @author auldanov on 15.08.2014.
 */
@Service
public class Universal2SimpleJoinSqlPartBuilder implements JoinSqlPartBuilder {

    @Autowired
    RefBookDao refBookDao;

    @Override
    public String build(String foreignTableAlias, JoinPoint joinPoint, boolean firstAttributeInChain) {
        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = joinPoint.getReferenceAttribute().getAlias();

        RefBook lastRefBook = refBookDao.getByAttribute(joinPoint.getDestinationAttribute().getId());

        // алиас аттрибута справочника который является ссылкой с учетом алиаса таблицы
        String linkAttributeAliasWithTableAlias = firstAttributeInChain ?
                "a" + linkAttributeAlias + ".reference_value" :
                foreignTableAlias + "." + linkAttributeAlias;

        // составления join запроса
        return new StringBuilder()
                .append("left join ")
                .append(lastRefBook.getTableName())
                .append(" ")
                .append(foreignTableAlias)
                .append(" on ")
                .append(foreignTableAlias)
                .append(".id = ")
                .append(linkAttributeAliasWithTableAlias)
                .append("\n")
                .toString();
    }
}
