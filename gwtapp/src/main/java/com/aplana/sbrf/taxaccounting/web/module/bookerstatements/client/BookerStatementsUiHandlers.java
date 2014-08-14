package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

/**
 * Хендлеры Формы фильтрации бухгалтерской отчётности
 *
 * @author Dmitriy Levykin
 */
public interface BookerStatementsUiHandlers extends AplanaUiHandlers {
    void onSearch();

    void onSortingChanged();

    void onCreateClicked();
}
