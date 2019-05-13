package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;

/**
 * Сервис для справочника "Подсистемы АС УН".
 */
public interface SubsystemService {

    /**
     * Получить список Подсистем с поиском по названию.
     *
     * @param name подстрока для поиска по названию Подсистем
     * @return список Подсистем, отвечающих критерию поиска
     */
    PagingResult<Subsystem> findByName(String name);
}
