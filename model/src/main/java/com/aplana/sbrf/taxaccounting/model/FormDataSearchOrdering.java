package com.aplana.sbrf.taxaccounting.model;

/**
 * Способы сортировки списка налоговых форм
 * @author dsultanbekov
 */
public enum FormDataSearchOrdering {
	/**
	 * По идентификатору
	 */
	ID,
	/**
	 * По типу
	 */
	KIND,
	/**
	 * По состоянию
	 */
	STATE,
	/**
	 * По наименованию вида формы 
	 */
	FORM_TYPE_NAME,
	/**
	 * По наименованию подразделения
	 */
	DEPARTMENT_NAME,
	/**
	 * По наименованию отчётного периода
	 */
	REPORT_PERIOD_NAME
}
