package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;

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

    /**
     * Найти адресообразующий объект
     * @param regionCode код региона (обязательный параметр)
     * @param area район
     * @param city город
     * @param locality населенный пункт
     * @param street улица
     * @return адресообразующий объект справочника
     */
    List<AddressObject> findAddress(String regionCode, String area, String city, String locality, String street);

    /**
     * Найти регион по коду
     * @param regionCode код региона
     * @return адресообразующий объект справочника
     */
    AddressObject findRegionByCode(String regionCode);

}
