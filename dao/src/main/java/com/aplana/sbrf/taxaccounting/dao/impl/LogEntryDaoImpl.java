package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.joda.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.QLogEntry.logEntry;
import static com.querydsl.core.types.Projections.bean;

@Repository
public class LogEntryDaoImpl extends AbstractDao implements LogEntryDao {

    final private SQLQueryFactory sqlQueryFactory;

    public LogEntryDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<LogEntry> logEntryBean = bean(LogEntry.class, logEntry.logId, logEntry.creationDate,
            logEntry.logLevel, logEntry.message, logEntry.object, logEntry.ord, logEntry.type);

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
            result.setDate(new org.joda.time.LocalDateTime(rs.getTimestamp("creation_date")).toDate());
            result.setLevel(LogLevel.fromId(rs.getInt("log_level")));

            String msg = rs.getString("message");
            result.setMessage(msg != null ? msg : "");
            result.setType(rs.getString("type"));
            result.setObject(rs.getString("object"));

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
     * @param logId      идентификатор группы сообщений
     * @param shift      сдвиг для {@link LogEntry#ord}
     */
    private void saveShift(List<LogEntry> logEntries, final String logId, final int shift) {
        final List<LogEntry> splitLogEntries = splitBigMessage(logEntries);

        String id = logId;
        int i = 0;
        for (LogEntry aLogEntry : splitLogEntries) {
            aLogEntry.setOrd(shift + i);
            i++;
            sqlQueryFactory.insert(logEntry)
                    .set(logEntry.logId, id)
                    .set(logEntry.creationDate, LocalDateTime.fromDateFields(aLogEntry.getDate()))
                    .set(logEntry.logLevel, (byte) aLogEntry.getLevel().getId())
                    .set(logEntry.type, aLogEntry.getType())
                    .set(logEntry.message, aLogEntry.getMessage())
                    .set(logEntry.ord, aLogEntry.getOrd())
                    .set(logEntry.object, aLogEntry.getObject())
                    .execute();

        }
    }

    @Override
    public List<LogEntry> get(String logId) {
        if (logId.isEmpty()) {
            return null;
        }

        List<LogEntry> result = getJdbcTemplate().query(
                "select log_id, ord, creation_date, log_level, message, type, object from log_entry where log_id = ? order by ord",
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
                        "t.log_id, t.ord, t.creation_date, t.log_level, t.message, t.type, t.object " +
                        "from " +
                        "(select l.*, row_number() " + (isSupportOver() ? "over(order by l.ord)" : "over()") + " as rn from log_entry l where l.log_id = ?) t " +
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
        Map<String, Object> paramMap = new HashMap<String, Object>();
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
        result.put(LogLevel.INFO, ((Number) countMap.get("INFO")).intValue());
        result.put(LogLevel.WARNING, ((Number) countMap.get("WARN")).intValue());
        result.put(LogLevel.ERROR, ((Number) countMap.get("ERROR")).intValue());

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
