package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;

import java.util.List;

/**
 * DAO для работы со справочником "Статусы налогоплательщика"
 */
public interface TaxPayerStateDao {
    /**
     * Найти все действующие записи
     */
    List<RefBookTaxpayerState> findAllActive();
}
