package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;

import java.util.List;

/**
 * DAO для работы со справочником "ОКСМ"
 */
public interface RefBookCountryDao {
    /**
     * Найти все действующие записи
     */
    List<RefBookCountry> findAllActive();
}
