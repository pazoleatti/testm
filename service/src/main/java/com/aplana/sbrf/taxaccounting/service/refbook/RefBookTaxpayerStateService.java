package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником "Статусы налогоплательщика"
 */
@ScriptExposed
public interface RefBookTaxpayerStateService {
    /**
     * Найти все действующие записи
     *
     * @return список действующих записей
     */
    List<RefBookTaxpayerState> findAllActive();
}