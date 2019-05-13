package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;

/**
 * Доступ к справочнику "Участники информационного обмена" SUBSYSTEM.
 */
public interface SubsystemDao {

    /**
     * Получить данные о Подсистеме по её идентификатору.
     *
     * @param id идентификатор Подсистемы
     * @return объект из базы / null, если объекта с таким id нет
     */
    Subsystem findById(long id);

    /**
     * Получить список Подсистем с поиском по названию.
     *
     * @param name подстрока для поиска по названию Подсистем
     * @return список Подсистем, отвечающих критерию поиска
     */
    PagingResult<Subsystem> findByName(String name);
}
