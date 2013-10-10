package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление типов параметров приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:24
 */

public enum ConfigurationParam {

	FORM_DATA_KEY_FILE ("Путь к файлу ключей ЭЦП для форм"),
	REF_BOOK_KEY_FILE ("Путь к файлу ключей ЭЦП для справочников"),
	FORM_DATA_DIRECTORY ("Путь папке транспортных файлов для форм"),
	REF_BOOK_DIRECTORY ("Путь папке транспортных файлов для справочников");

	private String caption;

	private ConfigurationParam(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

}
