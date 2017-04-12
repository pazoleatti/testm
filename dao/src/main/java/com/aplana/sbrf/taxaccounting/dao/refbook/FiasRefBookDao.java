package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;

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
     *
     * @param tableName имя таблицы справочника
     * @param records   пакет строк, количество строк в пакете определяется в скрипте загрузки
     */
    void insertRecordsBatch(String tableName, List<Map<String, Object>> records);

    /**
     * Удалить данныее из всех таблиц ФИАС
     */
    void clearAll();

    /**
     * Найти идентификаторы всех адресообразующих обектов в справочнике ФИАС соответствующие адресам указанным в РНУ-НДФЛ
     *
     * @param declarationId идентификатор декларации
     * @return карта идентификатор записи РНУ-НДФЛ - идентификатор адреса в справочнике ФИАС
     */
    Map<Long, Long> checkAddressByFias(Long declarationId, int p_check_type);

    /**
     * Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
     */
    Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationId, int p_check_type);


}
