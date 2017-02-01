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
    REPORT_PERIOD_YEAR,
    /**
     * АСНУ
     */
    ASNU,
    /**
     * По наименованию вида налоговой формы
     */
    DECLARATION_KIND_NAME,
    /**
     * Файл
     */
    FILE_NAME,
    /**
     * Примечание
     */
    NOTE,
    /**
     * Статус ЭД
     */
    DOC_STATE
}
