package com.aplana.sbrf.taxaccounting.dao.script.impl.dictionary;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;

import java.sql.Types;
import java.util.List;

public abstract class AbstractDictionaryDaoImpl extends AbstractDao {
    public abstract String getQueryFrom();

    protected Boolean isExist(String whereClause, Object[] values, Integer [] types) {
        String query = "SELECT count(*) FROM " + this.getQueryFrom() + " WHERE " + whereClause;
        return getJdbcTemplate().queryForInt(query, values, types) > 0;
    }

    protected List getData(String whereClause, Object[] values, Integer [] types) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
        query.append(getQueryFrom());
        query.append(" WHERE ");
        query.append(whereClause);
        return getJdbcTemplate().queryForList(query.toString(), values, types);
    }
}
