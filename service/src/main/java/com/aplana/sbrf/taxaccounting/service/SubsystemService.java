package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;

/**
 * Сервис для справочника "Подсистемы АС УН".
 */
public interface SubsystemService {

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
