package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
public class LogEntryDaoImpl extends AbstractDao implements LogEntryDao {

    private static final class LogEntryMapper implements RowMapper<LogEntry> {
        @Override
        public LogEntry mapRow(ResultSet rs, int index) throws SQLException {
            LogEntry result = new LogEntry();

            result.setLogId(rs.getString("log_id"));
            result.setOrd(rs.getInt("ord"));
            result.setDate(rs.getDate("creation_date"));
            result.setLevel(LogLevel.fromId(rs.getInt("log_level")));
            result.setMessage(rs.getString("message"));

            return result;
        }
    }

    @Override
    public void save(final List<LogEntry> logEntries, final String logId) {
        saveShift(logEntries, logId, 0);
    }

    @Override
    public void update(List<LogEntry> logEntries, String logId, boolean first) {
        int shift;

        if (first) {
            Integer minIndex = minOrder(logId);
            shift = (minIndex != null ? minIndex : 0) - logEntries.size();
        } else {
            Integer maxIndex = maxOrder(logId);
            shift = (maxIndex != null ? maxIndex : 0) + 1;
        }

        saveShift(logEntries, logId, shift);
    }

    private void saveShift(final List<LogEntry> logEntries, final String logId, final int shift) {
        getJdbcTemplate().batchUpdate(
                "insert into log_entry (log_id, ord, creation_date, log_level, message) values (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        LogEntry logEntry = logEntries.get(i);
                        logEntry.setLogId(logId);
                        logEntry.setOrd(shift + i);

                        ps.setString(1, logEntry.getLogId());
                        ps.setInt(2, logEntry.getOrd());
                        ps.setDate(3, new java.sql.Date(logEntry.getDate().getTime()));
                        ps.setInt(4, logEntry.getLevel().getId());
                        ps.setString(5, logEntry.getMessage());
                    }

                    @Override
                    public int getBatchSize() {
                        return logEntries.size();
                    }
                });
    }

    @Override
    public List<LogEntry> get(String logId) {
        if (logId.isEmpty()) {
            return null;
        }

        List<LogEntry> result = getJdbcTemplate().query(
                "select * from log_entry where log_id = ?",
                new Object[]{logId},
                new int[]{Types.VARCHAR},
                new LogEntryMapper()
        );

        if (result.isEmpty()) {
            throw new DaoException(String.format("Не удалось получить запись с id = %s", logId));
        }

        return result;
    }

    @Override
    public PagingResult<LogEntry> get(@NotNull String logId, int offset, int length) {
        List<LogEntry> records = getJdbcTemplate().query(
                "select " +
                        "t.log_id, t.ord, t.creation_date, t.log_level, t.message " +
                "from " +
                        "(select l.*, row_number() " + (isSupportOver()?"over(order by l.ord)":"over()") +  " as rn from log_entry l where l.log_id = ?) t " +
                "where " +
                        "t.rn between ? and ? " +
                "order by t.rn ",
                new Object[]{logId, offset + 1, offset + length},
                new int[]{Types.VARCHAR, Types.INTEGER, Types.INTEGER},
                new LogEntryMapper()
        );

        return new PagingResult<LogEntry>(records, count(logId));
    }

    public int count(@NotNull String logId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from log_entry where log_id = ?",
                new Object[]{logId},
                new int[]{Types.VARCHAR},
                Integer.class);
    }

    @Override
    public Integer minOrder(@NotNull String logId) {
        return getJdbcTemplate().queryForObject(
                "select min(ord) from log_entry where log_id = ?",
                new Object[]{logId},
                Integer.class
        );
    }

    @Override
    public Integer maxOrder(@NotNull String logId) {
        return getJdbcTemplate().queryForObject(
                "select max(ord) from log_entry where log_id = ?",
                new Object[]{logId},
                Integer.class
        );
    }
}
