package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс
 *
 * @author auldanov
 */
@Repository
class DBUtilsImpl extends AbstractDao implements DBUtils {
    @Override
    public List<Long> getNextDataRowIds(int count) {
        return getNextIds(Sequence.FORM_DATA_NNN, count);
    }

    @Override
    public List<Long> getNextRefBookRecordIds(int count) {
        return getNextIds(Sequence.REF_BOOK_RECORD, count);
    }

    @Override
    public List<Long> getNextIds(Sequence sequence, int count) {
        if (isSupportOver())
            return getJdbcTemplate().queryForList("SELECT " + sequence.getName() + ".NEXTVAL FROM DUAL CONNECT BY LEVEL<= ?", new Object[]{count}, java.lang.Long.class);
        else {
            ArrayList<Long> listIds = new ArrayList<Long>(count);
            for (Integer i = 0; i < count; i++)
                listIds.add(getJdbcTemplate().queryForObject("SELECT " + sequence.getName() + ".NEXTVAL FROM DUAL", Long.class));
            return listIds;
        }
    }

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection(getJdbcTemplate().getDataSource());
    }

    @Override
    public void checkConnection() {
        getJdbcTemplate().queryForObject("SELECT 1 FROM DUAL", Integer.class);
    }
}