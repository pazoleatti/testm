package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
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
    void onCommonAddRow(Integer index);

    /**
     * Добавление строки в таболицу параметров загрузки НФ
     */
    void onFormAddRow(Integer index);

    /**
     * Проверка достуности путей, указанных в выделенной строкеы
     */
    void onCheckReadWriteAccess(DataRow<Cell> selRow, boolean common);
}
