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
     * @return Список значений справочника отсортированых по названию по возрастанию
     */
    List<RefBookAsnu> findAll();

    /**
     * Возвращяет записи справочника по идентификаторам
     *
     * @param ids идентификаторы
     * @return список записей справочника
     */
    List<RefBookAsnu> findAllByIdIn(List<Long> ids);

    /**
     * Возвращяет записи справочника по идентификатору
     *
     * @param id идентификатор
     * @return запись справочника или null
     */
    RefBookAsnu findById(Long id);

    /**
     * Возвращяет запись справочника по наименованию
     *
     * @param name наименование АСНУ
     * @return запись справочника или null
     */
    RefBookAsnu findByName(String name);
}
