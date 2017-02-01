package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DAO для хранения списков LogEntry в таблице BLOB_DATA
 *
 * @author Stanislav Yasinskiy
 */
public interface LogEntryDao {

    /**
     * Создание записей в логах
     *
     * @param logEntries список новых логов
     * @param logId идентификатор группы логов
     */
    void save(List<LogEntry> logEntries, String logId);

    /**
     * Добавляет логи в уже существующую группу
     *
     * @param logEntries список новых логов
     * @param logId идентификатор группы логов
     * @param first добавить в начало списка
     */
    void update(List<LogEntry> logEntries, String logId, boolean first);

    /**
     * Получение списка логов по идентификатору группы
     *
     * @param logId идентификатор группы логов
     */
    List<LogEntry> get(@NotNull String logId);

    /**
     * Получение страницы логов по идентификатору группы
     *
     * @param logId идентификатор группы логов
     * @param length размер страницы
     * @param offset смещение
     */
    PagingResult<LogEntry> get(@NotNull String logId, int offset, int length);

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
}
