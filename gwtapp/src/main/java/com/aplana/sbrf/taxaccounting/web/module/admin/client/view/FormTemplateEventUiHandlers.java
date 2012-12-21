package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface FormTemplateEventUiHandlers extends UiHandlers {
	void removeEventScript(FormDataEvent event, Script script);
	void addEventScript(FormDataEvent event, Script script);
	List<Script> getScriptsByEvent(FormDataEvent event);
	List<Script> getScripts();
}
