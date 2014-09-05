package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Реализация строителя join части sql выражения
 * когда простой справочник ссылается на другой простой справочник
 *
 * @author auldanov on 22.08.2014.
 */
@Service
public class Simple2SimpleJoinSqlPartBuilder implements JoinSqlPartBuilder {

    @Autowired
    RefBookDao refBookDao;

    @Override
    public String build(String foreignTableAlias, JoinPoint joinPoint, boolean firstAttributeInChain) {
        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = joinPoint.getReferenceAttribute().getAlias();

        RefBook lastRefBook = refBookDao.getByAttribute(joinPoint.getDestinationAttribute().getId());

        // составления join запроса
        return new StringBuilder()
                .append("left join ")
                .append(lastRefBook.getTableName())
                .append(" ")
                .append(foreignTableAlias)
                .append(" on ")
                .append(foreignTableAlias)
                .append(".id = ")
                .append(linkAttributeAlias)
                .append("\n")
                .toString();
    }
}
