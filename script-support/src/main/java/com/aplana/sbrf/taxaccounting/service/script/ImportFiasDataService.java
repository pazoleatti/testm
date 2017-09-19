package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

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
     * Удаляет данные из всех справочников
     */
    void clearAll();

    /**
     * Удаляет индексы из таблицы FIAS_ADDROBJ для ускорения импорта
     */
    void dropIndexes();

    /**
     * Восстанавливает индексы из таблицы FIAS_ADDROBJ для ускорения импорта
     */
    void createIndexes();
}
