package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.TaxChangesEvent;

import java.util.List;

/**
 * Дао для обработки событий из УН на стороне НДФЛ
 * @author dloshkarev
 */
public interface TaxEventDao {

    /**
     * Проверяет появление новых необработанных событий из УН путем сравнения таблицы LOG_TABLE_CHANGE_PROCESSED и VW_LOG_TABLE_CHANGE.
     * Если в LOG_TABLE_CHANGE_PROCESSED нет записей, которые есть в VW_LOG_TABLE_CHANGE, значит на стороне НФДЛ они еще не обработаны
     * @return список новых необработанных событий из УН
     */
    List<TaxChangesEvent> getNewTaxEvents();

    /**
     * Сохраняет в таблицу LOG_TABLE_CHANGE_PROCESSED в БД НДФЛ отметку о том, что событие из УН было обработано
     * @param event событие из УН
     */
    void processTaxEvent(TaxChangesEvent event);
}
