package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
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
     * @param uuid         идентификатор группы логов
     * @param pagingParams Параметры пейджинга
     * @return
     */
    PagingResult<LogEntry> fetch(String uuid, PagingParams pagingParams);

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
     * @return uuid null, если ошибок нет
     */
    String save(List<LogEntry> logEntry);

    /**
     * Число ошибок каждой из групп ERROR, WARNING и INFO
     *
     * @param uuid
     * @return
     */
    Map<LogLevel, Integer> getLogCount(String uuid);

    /**
     * Обновить ранее сохраненный список сообщений, добавив новые логи в конец имеющихся.
     *
     * @param logEntries новые сообщения
     * @param uuid       идентификатор записи
     * @return
     */
    String update(List<LogEntry> logEntries, String uuid);

    /**
     * Обновить ранее сохраненный список сообщений, добавив новые логи в начало имеющихся.
     *
     * @param logEntries новые сообещения
     * @param uuid       идентификатор записи
     * @return идентификатор записи
     */
    String addFirst(List<LogEntry> logEntries, String uuid);
}