package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateImpexUiHandlers extends UiHandlers {
	void uploadFormTemplateSuccess(String uuid);
	void uploadFormTemplateFail(String msg);
	void downloadFormTemplate();
    void uploadDectResponseWithErrorUuid(String uuid);
}
