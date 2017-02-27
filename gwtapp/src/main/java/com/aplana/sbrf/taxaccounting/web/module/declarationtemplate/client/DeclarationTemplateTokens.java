package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

public final class DeclarationTemplateTokens {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private DeclarationTemplateTokens() {
	}

	/**
	 * Имена страниц
	 */

    public static final String declarationTemplateInfo = "!declarationTemplateInfo";
    public static final String declarationTemplateInfoLabel = "Основная информация";
    public static final int declarationTemplateInfoPriority = 0;

    public static final String declarationTemplateScript = "!declarationTemplateScript";
    public static final String declarationTemplateScriptLabel = "Скрипт";
    public static final int declarationTemplateScriptPriority = 1;

    public static final String declarationTemplateSubreports = "!declarationTemplateSubreports";
    public static final String declarationTemplateSubreportsLabel = "Специфические отчеты";
    public static final int declarationTemplateSubreportsPriority = 2;

    public static final String declarationTemplateFiles = "!declarationTemplateFiles";
    public static final String declarationTemplateFilesLabel = "Файлы";
    public static final int declarationTemplateFilesPriority = 3;

    public static final String declarationTemplate = "!declarationTemplate";
    public static final String declarationVersionList = "!declarationVersionList";
	public static final String declarationTemplateList = "!declarationTemplateList";
	public static final String declarationTemplateId = "declarationTemplateId";
    public static final String declarationType = "declarationType";
}
