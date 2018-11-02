package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;

import java.util.List;

/**
 * DAO для работы со справочником "Коды документов"
 */
public interface RefBookDocTypeDao {
    /**
     * Найти все действующие записи
     *
     * @return список действующих записей
     */
    List<RefBookDocType> findAllActive();
}