package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import com.aplana.sbrf.taxaccounting.model.Script;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateScriptUiHandlers extends UiHandlers {
	void createScript();
	void deleteScript(Script selectedScript);
}
