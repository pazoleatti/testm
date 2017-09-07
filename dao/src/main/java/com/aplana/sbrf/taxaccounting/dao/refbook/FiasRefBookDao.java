package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;

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
     * @return карта идентификатор записи РНУ-НДФЛ - результат проверки адреса (идентификатор адреса в справочнике ФИАС и поля chk_*)
     */
    Map<Long, FiasCheckInfo> checkAddressByFias(Long declarationId, int p_check_type);

    /**
     * Выполнить проверку на наличие/отсутствие в ФИАС элементов адресов, указанных в декларации
     */
    Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationId, int p_check_type);

    /**
     * Вызывает процедуру обновляющую материальные представления после импорта в ФИАС
     */
    public void refreshViews();

    /**
     * Удаляет индексы из таблицы FIAS_ADDROBJ для ускорения импорта
     */
    void dropIndexes();

    /**
     * Восстанавливает индексы из таблицы FIAS_ADDROBJ для ускорения импорта
     */
    void createIndexes();

}
