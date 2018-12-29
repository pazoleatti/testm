package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;

import java.util.List;

/**
 * DAO для работы со справочником "Коды документов"
 */
public interface RefBookDocTypeDao {
    /**
     * Возвращяет все действующие записи
     *
     * @return список действующих записей справочника
     */
    List<RefBookDocType> findAllActive();

    /**
     * Возвращяет список записей справочника по списку идентификаторов
     *
     * @param ids список идентификаторов
     * @return список записей справочника
     */
    List<RefBookDocType> findAllByIdIn(List<Long> ids);
}