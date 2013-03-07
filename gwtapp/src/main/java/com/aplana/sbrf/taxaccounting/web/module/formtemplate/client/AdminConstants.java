package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client;


/**
 * Класс констант
 */

public final class AdminConstants {

	/**
	 * Имена страниц
	 */
	public static final class NameTokens {
		public static final String adminPage = "!admin";
		public static final String formTemplateScriptPage = "!formTemplateScript";
		public static final String formTemplateEventPage = "!formTemplateEvent";
		public static final String formTemplateColumnPage = "!formTemplateColumn";
		public static final String formTemplateRowPage = "!formTemplateRow";
		public static final String formTemplateInfoPage = "!formTemplateInfo";
		public static final String formTemplateStylePage = "!formTemplateStyle";
		public static final String formTemplateMainPage = "!formTemplateMain";
		public static final String formTemplateId = "formTemplateId";
	}

	/**
	 * Порядок отображения табов в таб панели
	 */
	public static final class TabPriorities {
		public static final int formTemplateInfoPriority = 0;
		public static final int formTemplateScriptPriority = 1;
		public static final int formTemplateEventPriority = 2;
		public static final int formTemplateColumnPriority = 3;
		public static final int formTemplateStylePriority = 4;
		public static final int formTemplateRowPriority = 5;
	}

	/**
	 * Имена табов в таб панели
	 */
	public static final class TabLabels {
		public static final String formTemplateInfoLabel = "Основая информация";
		public static final String formTemplateScriptLabel = "Скрипты";
		public static final String formTemplateEventLabel = "События";
		public static final String formTemplateColumnLabel = "Описание столбцов";
		public static final String formTemplateStyleLabel = "Наборы стилей";
		public static final String formTemplateRowLabel = "Начальные данные";
	}
}
