package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataListUiHandlers extends UiHandlers{
	
	/**
	 * Изменились параметры сортировки таблицы
	 */
	void onSortingChanged();
	
	/**
	 * Выю пытается обновить данные в таблице
	 * 
	 * @param start
	 * @param length
	 */
	void onRangeChange(int start, int length);
	
}
