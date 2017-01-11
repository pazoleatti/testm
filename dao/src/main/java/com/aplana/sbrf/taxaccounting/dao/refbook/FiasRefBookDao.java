package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * DAO для работы со справочниками ФИАС
 *
 * @author Andrey Drunk
 */
public interface FiasRefBookDao {

    /**
     * Пакетное добавление строк справочника ФИАС
     * @param tableName имя таблицы справочника
     * @param records пакет строк, количество строк в пакете определяется в скрипте загрузки
     */
    void insertRecordsBatch(String tableName, List<Map<String, Object>> records);

    /**
     * Удалить данныее из всех таблиц ФИАС
     */
    void clearAll();

}
