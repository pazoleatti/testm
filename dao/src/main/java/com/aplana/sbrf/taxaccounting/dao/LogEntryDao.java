package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * DAO для хранения списков LogEntry в таблице BLOB_DATA
 *
 * @author Stanislav Yasinskiy
 */
public interface LogEntryDao {

    /**
     * Удаляет все записи в логе по ид
     *
     * @param logId идентификатор лога
     */
    void deleteByLogId(String logId);

    /**
     * Создание записей в логах
     *
     * @param logEntries список новых логов
     * @param logId      идентификатор группы логов
     */
    void save(List<LogEntry> logEntries, String logId);

    /**
     * Добавляет логи в уже существующую группу
     *
     * @param logEntries список новых логов
     * @param logId      идентификатор группы логов
     * @param first      добавить в начало списка
     */
    void update(List<LogEntry> logEntries, String logId, boolean first);

    /**
     * Получение списка логов по идентификатору группы
     *
     * @param logId идентификатор группы логов
     * @return список логов
     */
    List<LogEntry> fetch(@NotNull String logId);

    /**
     * Получение страницы логов по идентификатору группы
     *
     * @param logId        идентификатор группы логов
     * @param pagingParams Параметры пейджинга
     */
    PagingResult<LogEntry> fetch(@NotNull String logId, PagingParams pagingParams);

    /**
     * Получает минимальное значение order у группы логов
     *
     * @param logId идентификатор группы логов
     * @return порядковый номер
     */
    Integer minOrder(@NotNull String logId);

    /**
     * Получает максимальное значение order у группы логов
     *
     * @param logId идентификатор группы логов
     * @return порядковый номер
     */
    Integer maxOrder(@NotNull String logId);

    /**
     * Расчитывает количество сообщений на каждом уровне важности
     *
     * @param logId идентификатор группы логов
     * @return ключ - уровень важности, значение количество сообщений
     */
    Map<LogLevel, Integer> countLogLevel(@NotNull String logId);
}
