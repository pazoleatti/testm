package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateListUiHandlers extends UiHandlers {
	/**
	 * Обработчик события выбора шаблона декларации.
	 *
	 * @param id идентификатор шаблона выбранной декларации.
	 */
	public void selectDeclaration(Integer id);
}
