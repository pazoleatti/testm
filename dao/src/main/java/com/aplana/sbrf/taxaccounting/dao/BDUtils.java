package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

/**
 * Интерфейс для класса реализуещего работу
 * связанной с не совместимостью тестового бд(hsqlbd) и
 * бд на продакшене(oracle)
 * @author auldanov
 */
public interface BDUtils {
    /**
     * Метод возвращает список зарезервированных id
     * по которым можно осуществлять вставку в таблицу data_row
     *
     * Размещена здесь, так как использует вызов хранимки,
     * которая отказывается наботать в hsql
     *
     * @param count
     * @return
     */
    List<Long> getNextIds(Long count);
}
