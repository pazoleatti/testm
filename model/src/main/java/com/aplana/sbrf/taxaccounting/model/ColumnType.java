package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип столбца налоговой формы
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.09.14 16:05
 */

public enum ColumnType {

	/** Автонумеруемая графа */
	AUTO ('A', "Автонумеруемая графа"),
	/** Дата */
	DATE ('D', "Дата"),
	/** Число */
	NUMBER ('N', "Число"),
	/** Справочник */
	REFBOOK ('R', "Справочник"),
	/** Зависимая графа */
	REFERENCE ('R', "Зависимая графа"),
	/** Строка */
	STRING ('S', "Строка");

	private char code;
	private String title;

	ColumnType(char code, String title) {
		this.code = code;
		this.title = title;
	}

	public char getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ColumnType{");
		sb.append(name());
		sb.append(";");
		sb.append(title);
		sb.append('}');
		return sb.toString();
	}
}
