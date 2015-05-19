package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы срезов строк налоговых форм
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 19.05.15 19:35
 */

public enum DataRowType {

	TEMP ("Временный срез", 1),
	STABLE ("Постоянный срез", 0),
	MANUAL ("Версия ручного ввода", 1),
	AUTO ("Автоматическая версия", 0);

	private String title;
	private int code;

	DataRowType(String title, int code) {
		this.title = title;
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public int getCode() {
		return code;
	}

}
