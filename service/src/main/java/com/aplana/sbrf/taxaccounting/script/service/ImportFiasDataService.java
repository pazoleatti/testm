package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;
import java.util.Map;

/**
 * Сервис импорта данных в справочники ФИАС
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface ImportFiasDataService {

    /**
     * Пакетная вставка записей в справочник
     */
    void insertRecords(String table, List<Map<String, Object>> records);

    /**
     * Удаляет индексы из таблицы FIAS_ADDROBJ для ускорения импорта и очищает её содержимое
     */
    void beforeImport();

    /**
     * Восстанавливает индексы в таблице FIAS_ADDROBJ после импорта данных для ускорения импорта
     */
    void afterImport();
}
