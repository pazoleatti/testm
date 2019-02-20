package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;

import java.util.List;

/**
 * Dao для работы со справочником Коды видов вычетов (ид=921)
 */
public interface RefBookDeductionTypeDao {

    /**
     * Возвращяет все записи справочника, актуальные на текущую дату
     *
     * @return список записей справочника
     */
    List<RefBookDeductionType> findAll();
}
