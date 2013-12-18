package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;

import java.util.List;
import java.util.Map;

/**
 * Сервис для хранения списков LogEntry в таблице BLOB_DATA
 *
 * @author Stanislav Yasinskiy
 */
public interface LogEntryService {

    /**
     * Получить LogEntry постранично
     *
     * @param uuid
     * @return
     */
    PagingResult<LogEntry> get(String uuid, int start, int length);

    /**
     * Получить LogEntry целиком
     *
     * @param uuid
     * @return
     */
    List<LogEntry> getAll(String uuid);

    /**
     * Сохранить LogEntry
     *
     * @param logEntry
     * @return uuid
     */
    String save(List<LogEntry> logEntry);

    /**
     * Число ошибок каждой из групп ERROR, WARNING и INFO
     *
     * @param uuid
     * @return
     */
    Map<LogLevel, Integer> getLogCount(String uuid);
}