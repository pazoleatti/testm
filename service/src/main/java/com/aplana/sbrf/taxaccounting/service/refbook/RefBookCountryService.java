package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником "Общероссийский классификатор стран мира"
 */
@ScriptExposed
public interface RefBookCountryService {
    /**
     * Найти все действующие записи
     *
     * @return список действующих записей
     */
    List<RefBookCountry> findAllActive();

    /**
     * Проверка существования записи по полю code.
     *
     * @param code код страны
     * @return существует ли запись в базе
     */
    boolean existsByCode(String code);
}
