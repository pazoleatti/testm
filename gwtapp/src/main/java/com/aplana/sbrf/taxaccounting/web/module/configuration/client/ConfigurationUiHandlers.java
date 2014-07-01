package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ConfigurationUiHandlers extends UiHandlers{

    /**
     * Сохранение изменений
     */
	void onSave();

    /**
     * Отмема изменений и обновление формы
     */
	void onCancel();

    /**
     * Добавление строки в таблицу общих параметров
     */
    void onCommonAddRow();

    /**
     * Добавление строки в таболицу параметров загрузки НФ
     */
    void onFormAddRow();
}
