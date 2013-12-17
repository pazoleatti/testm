package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

/**
 * DAO для хранения списков LogEntry в таблице BLOB_DATA
 *
 * @author Stanislav Yasinskiy
 */
public interface LogEntryDao {

    /**
     * Создание записи
     *
     * @param logEntries
     * @param uuid
     */
    void save(List<LogEntry> logEntries, String uuid);

    /**
     * Получение записи
     *
     * @param uuid
     * @return
     */
    List<LogEntry> get(String uuid);
}
