package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;

import java.util.Date;
import java.util.List;

/**
 * Dao для работы со справочником Коды видов вычетов (ид=921)
 */
public interface RefBookDeductionTypeDao {

    /**
     * Возвращяет все записи справочника, актуальные на текущую дату
     *
     * @param version дата актуальности записи
     * @return список записей справочника
     */
    List<RefBookDeductionType> findAllByVersion(Date version);
}
