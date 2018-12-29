package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;

import java.util.List;

/**
 * DAO для работы со справочником "Статусы налогоплательщика"
 */
public interface RefBookTaxpayerStateDao {
    /**
     * Найти все действующие записи
     */
    List<RefBookTaxpayerState> findAllActive();

    /**
     * Возвращяет список записей справочника по списку идентификаторов
     *
     * @param ids список идентификаторов
     * @return список записей справочника
     */
    List<RefBookTaxpayerState> findAllByIdIn(List<Long> ids);
}
