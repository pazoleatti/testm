package com.aplana.sbrf.taxaccounting.model;

/**
 * Способы сортировки списка налоговых форм
 * @author srybakov
 */
public enum DeclarationSearchOrdering {
	/**
	 * По идентификатору
	 */
	ID,
	/**
	 * По наименованию подразделения
	 */
	DEPARTMENT_NAME,
	/**
	 * По наименованию отчётного периода
	 */
	REPORT_PERIOD_NAME
}
