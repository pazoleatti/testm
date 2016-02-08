package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы срезов строк налоговых форм
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 19.05.15 19:35
 */

public enum DataRowType {
	/** Резервный срез */
	TEMP (1),
	/** Основной срез */
	SAVED (0),
	/** Версия ручного ввода */
	MANUAL (1),
	/** Автоматическая версия */
	AUTO (0);

	private int code;

	DataRowType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
