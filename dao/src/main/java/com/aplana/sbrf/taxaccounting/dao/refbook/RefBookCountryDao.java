package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;

import java.util.List;

/**
 * DAO для работы со справочником "ОКСМ"
 */
public interface RefBookCountryDao {
    /**
     * Возвращяет все действующие записи
     *
     * @return список действующих записей
     */
    List<RefBookCountry> findAllActive();

    /**
     * Возвращяет список записей справочника по списку идентификаторов
     *
     * @param ids список идентификаторов
     * @return список записей справочника
     */
    List<RefBookCountry> findAllByIdIn(List<Long> ids);
}