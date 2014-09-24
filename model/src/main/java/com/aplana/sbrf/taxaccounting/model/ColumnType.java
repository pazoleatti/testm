package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип столбца налоговой формы
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.09.14 16:05
 */

public enum ColumnType {

	AUTO ('A'),
	DATE ('D'),
	NUMBER ('N'),
	REFBOOK ('R'),
	REFERENCE ('R'),
	STRING ('S');

	private char code;

	ColumnType(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}

}
