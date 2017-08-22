package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.List;

/**
 * Сервис для работы со справочником АСНУ
 */
public interface RefBookAsnuService {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookAsnu> fetchAllAsnu();
}
