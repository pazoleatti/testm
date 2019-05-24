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
    public boolean existsById(String id) {
        return getJdbcTemplate().queryForObject("select case when exists(select * from log where id = ?) then 1 else 0 end from dual", Boolean.class, id);
    }

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
            return getJdbcTemplate().update("" +
                    "delete from log where id in (\n" +
                    "  select log.id from LOG\n" +
                    "  left join NOTIFICATION on NOTIFICATION.LOG_ID = LOG.ID\n" +
                    "  left join LOG_BUSINESS on LOG_BUSINESS.LOG_ID = LOG.ID\n" +
                    "  where LOG_BUSINESS.ID is null and NOTIFICATION.ID is null and\n" +
                    "    (systimestamp - LOG.creation_date) > numtodsinterval(24, 'hour')" +
                    ")");
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Ошибка при удалении устаревших записей таблицы LOG. %s.", e.getMessage()));
        }
    }
}
