package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы со справочником Коды видов вычетов (ид=921)
 */
@ScriptExposed
public interface RefBookDeductionTypeService {

    /**
     * Возвращяет все записи справочника
     *
     * @param version дата актуальности записи
     * @return список записей справочника
     */
    List<RefBookDeductionType> findAllByVersion(Date version);
}
