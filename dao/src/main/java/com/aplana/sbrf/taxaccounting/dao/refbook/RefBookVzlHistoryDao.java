package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Дао для истории взаимозависимых лиц
 * @author dloshkarev
 */
public interface RefBookVzlHistoryDao {
    Long REF_BOOK_ID = 521L;
    String TABLE_NAME = "REF_BOOK_VZL_HISTORY";

    /**
     * Получение записи справочника по уникальному идентификатору
     * @param uniqueRecordId уникальный идентификатор
     * @return список значений атрибутов записи
     */
    Map<String, RefBookValue> getRecordData(Long uniqueRecordId);

    /**
     * Создает новые записи в справочнике
     * @param records список новых записей
     */
    void createRecords(@NotNull List<Map<String, RefBookValue>> records);

    /**
     * Обновляет значения атрибутов записи
     * @param uniqueRecordId уникальный идентификатор
     * @param record новые значения атрибутов
     */
    void updateRecord(Long uniqueRecordId, Map<String, RefBookValue> record);
}
