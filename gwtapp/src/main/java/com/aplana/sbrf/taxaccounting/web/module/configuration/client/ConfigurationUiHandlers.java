package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface ConfigurationUiHandlers extends UiHandlers{

    /**
     * Сохранение изменений
     */
	void onSaveClicked();

    /**
     * Отмема изменений и обновление формы
     */
	void onCancel();

    /**
     * Добавление строки в таблицу общих параметров
     */
    void onAddRow();

    /**
     * Проверка достуности путей, указанных в выделенной строке
     */
    void onCheckAccess(boolean needSaveAfter);

    List<DataRow<Cell>> getRowsData(ConfigurationParamGroup group);

    void onDeleteItem();
}
