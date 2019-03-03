package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником "Коды документов"
 */
@ScriptExposed
public interface RefBookDocTypeService {
    /**
     * Найти все действующие записи
     *
     * @return список действующих записей
     */
    List<RefBookDocType> findAllActive();

    /**
     * Проверка существования записи по полю code.
     *
     * @param code код документа
     * @return существует ли запись в базе
     */
    boolean existsByCode(String code);
}
