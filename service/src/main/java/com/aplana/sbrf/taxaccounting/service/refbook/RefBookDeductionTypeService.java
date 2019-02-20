package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником Коды видов вычетов (ид=921)
 */
@ScriptExposed
public interface RefBookDeductionTypeService {

    /**
     * Возвращяет все записи справочника
     *
     * @return список записей справочника
     */
    List<RefBookDeductionType> findAll();
}
