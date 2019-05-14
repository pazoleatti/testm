package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

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
     */
    PagingResult<LogEntry> fetch(String uuid, PagingParams pagingParams);

    /**
     * Получить LogEntry целиком
     */
    List<LogEntry> getAll(String uuid);

    /**
     * Создаёт пустой лог в БД для дальнейшего его заполнения через {@link #save(Logger)}
     *
     * @return логгер
     */
    Logger createLogger();

    /**
     * Сохраняет сообщения логгера. Можно вызывать несколько раз для одного и того же логгера.
     *
     * @return uuid сохраненного лога
     */
    String save(Logger logger);

    /**
     * Сохранить LogEntry
     *
     * @return uuid сохраненного лога. Null если логи пустые
     */
    String save(List<LogEntry> logEntry);

    /**
     * Число ошибок каждой из групп ERROR, WARNING и INFO
     */
    Map<LogLevel, Integer> getLogCount(String uuid);

    /**
     * Обновить ранее сохраненный список сообщений, добавив новые логи в конец имеющихся.
     *
     * @param logEntries новые сообщения
     * @param uuid       идентификатор записи
     */
    String update(List<LogEntry> logEntries, String uuid);

    /**
     * Обновить ранее сохраненный список сообщений, добавив новые логи в начало имеющихся.
     *
     * @param logEntries новые сообещения
     * @param uuid       идентификатор записи
     * @return идентификатор записи
     * @deprecated не использовали метод с 2015 года, может и не стоит?
     * todo: кандидат на удаление и рефакторинг вниз по реализации.
     */
    @Deprecated
    String addFirst(List<LogEntry> logEntries, String uuid);
}