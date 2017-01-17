package com.aplana.sbrf.taxaccounting.model;

/**
 * Способы сортировки списка налоговых форм
 * @author srybakov
 */
public enum DeclarationDataSearchOrdering {
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
	REPORT_PERIOD_NAME,
	/**
	 * По наименованию типа декларации
	 */
	DECLARATION_TYPE_NAME,
    /**
     * По состоянию декларации
     */
    DECLARATION_STATE,
    /**
     * По году периода
     */
    REPORT_PERIOD_YEAR
}
