package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogDao;
import org.springframework.stereotype.Repository;


/**
 * Реализация DAO для группы логов
 *
 * @author pmakarov
 */

@Repository
public class LogDaoImpl extends AbstractDao implements LogDao {

    @Override
    public void save(String logId) {
        getJdbcTemplate().update(
                "insert into log (id, creation_date) values (?, ?)",
                logId,
                new java.sql.Date(new java.util.Date().getTime())
        );
    }
}
