package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
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
    void onAddRow(ConfigurationParamGroup group, Integer index);

    /**
     * Проверка достуности путей, указанных в выделенной строке
     */
    void onCheckAccess(ConfigurationParamGroup group, DataRow<Cell> selRow, boolean needSaveAfter);


}
