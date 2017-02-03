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
import java.util.*;

@Repository
public class LogEntryDaoImpl extends AbstractDao implements LogEntryDao {

    /**
     * Максимальный размер сообщения
     */
    private static final int MAX_MESSAGE_SIZE = 2000;

    private static final class LogEntryMapper implements RowMapper<LogEntry> {
        @Override
        public LogEntry mapRow(ResultSet rs, int index) throws SQLException {
            LogEntry result = new LogEntry();

            result.setLogId(rs.getString("log_id"));
            result.setOrd(rs.getInt("ord"));
            result.setDate(new Date(rs.getTimestamp("creation_date").getTime()));
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

    /**
     * Проходит по всем сообщениями:
     * сообщения больше {@link LogEntryDaoImpl#MAX_MESSAGE_SIZE} разбиваются на мелкие сообщения.
     */
    private List<LogEntry> splitBigMessage(List<LogEntry> logEntries) {
        List<LogEntry> entryList = new ArrayList<LogEntry>(logEntries);
        ListIterator<LogEntry> listIterator = entryList.listIterator();

        while (listIterator.hasNext()) {
            LogEntry logEntry = listIterator.next();

            if (logEntry.getMessage().length() <= MAX_MESSAGE_SIZE) {
                continue;
            }

            String[] messages = logEntry.getMessage().split("(?s)(?<=\\G.{" + MAX_MESSAGE_SIZE + "})");

            listIterator.remove();

            for (String message : messages) {
                LogEntry newLogEntry = new LogEntry(logEntry);
                newLogEntry.setMessage(message);

                listIterator.add(newLogEntry);
            }
        }

        return entryList;
    }

    /**
     * Сохраняет список сообщений в базу.
     * Большие сообщения разбиваются на мелкие.
     * {@link LogEntry#ord} проставляется с учетом сдвига.
     *
     * @param logEntries список сообщений
     * @param logId идентификатор группы сообщений
     * @param shift сдвиг для {@link LogEntry#ord}
     */
    private void saveShift(List<LogEntry> logEntries, final String logId, final int shift) {
        final List<LogEntry> splitLogEntries = splitBigMessage(logEntries);

        getJdbcTemplate().batchUpdate(
                "insert into log_entry (log_id, ord, creation_date, log_level, message) values (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        LogEntry logEntry = splitLogEntries.get(i);
                        logEntry.setLogId(logId);
                        logEntry.setOrd(shift + i);

                        ps.setString(1, logEntry.getLogId());
                        ps.setInt(2, logEntry.getOrd());
                        ps.setTimestamp(3, new java.sql.Timestamp(logEntry.getDate().getTime()));
                        ps.setInt(4, logEntry.getLevel().getId());
                        ps.setString(5, logEntry.getMessage());
                    }

                    @Override
                    public int getBatchSize() {
                        return splitLogEntries.size();
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
    public Map<LogLevel, Integer> countLogLevel(@NotNull String logId) {
        Map<String, Object> paramMap =  new HashMap<String, Object>();
        paramMap.put("logId", logId);

        Map<String, Object> countMap = getNamedParameterJdbcTemplate().queryForMap(
                "select " +
                    "count(case when l.log_level = 0 then 1 end) INFO, " +
                    "count(case when l.log_level = 1 then 1 end) WARN, " +
                    "count(case when l.log_level = 2 then 1 end) ERROR " +
                "from " +
                    "log_entry l " +
                "where " +
                    "log_id = :logId",
                paramMap
        );

        Map<LogLevel, Integer> result = new HashMap<LogLevel, Integer>();
        result.put(LogLevel.INFO, ((Number)countMap.get("INFO")).intValue());
        result.put(LogLevel.WARNING, ((Number)countMap.get("WARN")).intValue());
        result.put(LogLevel.ERROR, ((Number)countMap.get("ERROR")).intValue());

        return result;
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
