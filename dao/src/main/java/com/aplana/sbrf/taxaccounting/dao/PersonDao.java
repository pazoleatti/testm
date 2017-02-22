package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

/**
 * DAO-Интерфейс для работы со справочником физ. лиц, для специфики по дубликатам.
 */
public interface PersonDao {

    /**
     * Переводит запись в статус дубликата
     * @param recordIds - идентификаторы записей
     * @param originalId - идентификатор ФЛ оригинала
     */
    void setDuplicate(List<Long> recordIds, Long originalId);

    /**
     * Меняем родителя (RECORD_ID) у дубликатов
     * @param recordIds
     * @param originalId
     */
    void changeRecordId(List<Long> recordIds, Long originalId);

    /**
     * Переводит запись в статус оригинала
     * @param recordIds - идентификаторы записей
     */
    void setOriginal(List<Long> recordIds);

    Long getOriginal(Long recordId);

    List<Long> getDuplicate(Long originalRecordId);
}