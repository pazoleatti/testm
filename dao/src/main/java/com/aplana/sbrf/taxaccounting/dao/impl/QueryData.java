package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Общая модель для работы с запросом,
 * содержит два свойства:
 * 1. Параметры запроса
 * 2. Тело sql запроса
 *
 * @author auldanov on 19.08.2014.
 */
public class QueryData {
    /** Параметры запроса */
    private MapSqlParameterSource parameterSource;

    /** Тело sql запроса */
    private String query;

    public MapSqlParameterSource getParameterSource() {
        return parameterSource;
    }

    public void setParameterSource(MapSqlParameterSource parameterSource) {
        this.parameterSource = parameterSource;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryData queryData = (QueryData) o;

        if (parameterSource != null ? !parameterSource.equals(queryData.parameterSource) : queryData.parameterSource != null)
            return false;
        if (query != null ? !query.equals(queryData.query) : queryData.query != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parameterSource != null ? parameterSource.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }
}
