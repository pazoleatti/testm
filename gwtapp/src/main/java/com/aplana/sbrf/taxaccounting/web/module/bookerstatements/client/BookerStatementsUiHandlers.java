package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры Формы фильтрации бухгалтерской отчётности
 *
 * @author Dmitriy Levykin
 */
public interface BookerStatementsUiHandlers extends UiHandlers {
    void onSearch();

    void onSortingChanged();

    void onCreateClicked();
}
