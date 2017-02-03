package com.aplana.sbrf.taxaccounting.web.module.commonparameter.client;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface CommonParameterUiHandlers extends UiHandlers{

    /**
     * Получение списка общих параметров в гриде
     */
    List<DataRow<Cell>> getRowsData();

    /**
     * Сохранение изменений
     */
	void onSaveClicked();

    /**
     * Отмема изменений и обновление формы
     */
	void onCancel();

    /**
     * Восстанавливает значения по умолчанию
     */
	void onRestore();
}
