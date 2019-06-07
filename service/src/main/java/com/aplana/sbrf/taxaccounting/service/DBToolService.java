package com.aplana.sbrf.taxaccounting.service;

/**
 * Сервис для выполнения задач по обслуживанию БД
 */
public interface DBToolService {
    /**
     * Сжать таблицы
     */
    void shrinkTables();
}
