package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author Vitalii Samolovskikh
 */
public interface FormTemplateUiHandlers extends UiHandlers {
	public void load();
	public void save();
	public void selectScript();
	public void createScript();
	public void deleteScript();
}
