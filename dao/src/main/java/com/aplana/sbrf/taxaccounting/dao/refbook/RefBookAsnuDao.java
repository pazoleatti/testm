package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.List;

/**
 * Дао для работы со справочником АСНУ
 */
public interface RefBookAsnuDao {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookAsnu> fetchAll();

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Идентификаторы
     * @return Список значений справочника
     */
    List<RefBookAsnu> fetchByIds(List<Long> ids);

    /**
     * Получение записи справочника по идентификатору
     *
     * @param id идентификатор
     * @return значение справочника или null
     */
    RefBookAsnu fetchById(Long id);
}
