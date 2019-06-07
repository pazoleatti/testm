package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DBToolsDao;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

@Repository
public class DBToolsDaoImpl extends AbstractDao implements DBToolsDao {
    @Override
    public void shrinkTables() {
        getJdbcTemplate().execute("call NDFL_TOOLS.shrink_tables()");
    }
}
