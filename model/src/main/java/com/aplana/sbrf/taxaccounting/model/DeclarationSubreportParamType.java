package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип параметра спец. отчета
 *
 * @author lhaziev
 */

public enum DeclarationSubreportParamType {

	/** Дата */
	DATE ('D', "Дата"),
	/** Число */
	NUMBER ('N', "Число"),
	/** Справочник */
	REFBOOK ('R', "Справочник"),
	/** Строка */
	STRING ('S', "Строка");

	private char code;
	private String title;

	DeclarationSubreportParamType(char code, String title) {
		this.code = code;
		this.title = title;
	}

	public char getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

}
