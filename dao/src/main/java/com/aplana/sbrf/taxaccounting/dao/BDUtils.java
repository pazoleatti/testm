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
     * Метод возвращает список зарезервированных id для таблицы data_row
     */
    public List<Long> getNextDataRowIds(Long count);

    /**
     * Метод возвращает список зарезервированных id для таблицы ref_book_record
     */
    public List<Long> getNextRefBookRecordIds(Long count);

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
    List<Long> getNextIds(String seqName, Long count);
}
