package com.aplana.sbrf.taxaccounting.dao.script.impl.dictionary;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;

import java.util.List;

public abstract class AbstractDictionaryDaoImpl extends AbstractDao {
    public abstract String getBaseQuery();

    protected Boolean isExist(String whereClause, Object[] values, Integer [] types) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT count(*) FROM ");
        query.append(getBaseQuery());
        query.append(" WHERE ");
        query.append(whereClause);
        return getJdbcTemplate().queryForInt(query.toString(), values, types) > 0;
    }
}
