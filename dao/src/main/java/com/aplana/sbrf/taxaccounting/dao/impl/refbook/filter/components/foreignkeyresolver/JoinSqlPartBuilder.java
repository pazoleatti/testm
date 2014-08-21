package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

/**
 * Объект содержащий логику построение Join запроса
 *
 * @author auldanov on 15.08.2014.
 */
public interface JoinSqlPartBuilder {
    String build(String foreignTableAlias, JoinPoint joinPoint, boolean firstAttributeInChain);
}
