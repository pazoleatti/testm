package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.dao.DataAccessException;
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
                "insert into log (id, creation_date) values (?, systimestamp)",
                logId
        );
    }

    @Override
    public int clean() {
        try {
            return getJdbcTemplate().update("delete from LOG where id not in " +
                    "(select LOG_ID from NOTIFICATION) " +
                    "and (systimestamp - LOG.creation_date) > numtodsinterval(24, 'hour')");
        } catch (DataAccessException e){
            throw new DaoException(String.format("Ошибка при удалении устаревших записей таблицы LOG. %s.", e.getMessage()));
        }
    }
}
